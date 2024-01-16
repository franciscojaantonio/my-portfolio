using Grpc.Core;
using Grpc.Net.Client;
using UtilsLibrary;


namespace TransactionManager.Services
{
    public class TransactionServerService : TransactionManagerService.TransactionManagerServiceBase
    {
        private string nick;
        private int idNum;
        private int epoch = 1;
        private int slotNum;
        private Dictionary<int, bool> slotHasValue = new();
        private Dictionary<int, List<string>> slots = new();
        private Dictionary<string, PaxosManagerService.PaxosManagerServiceClient> leaseManagers;
        private Dictionary<string, TransactionManagerInnerService.TransactionManagerInnerServiceClient> transactionManagers;

        // Estrutura do dicionario:
        // string: Key (DadInt.key)
        // tuple: Value do dicionario é um tuplo com 2 elementos:
        // Value: primeiro elemento do tuplo que é o valor do determinado DadInt
        // TxMRow: segundo elemento do tuplo, é uma lista que é a fila dos TxMs que querem uma lease para o determinado DadInt
        // A lista TxMRow é composta por 2 elementos, Stamp que é um stamp único de cada lease, e o id do TxM (TxMID)
        private Dictionary<string, List<(int round, string TxMID)>> LeaseLog = new();
        private Dictionary<string, int> dadInts = new();

        public TransactionServerService(int idNum, string nick, int slotTime, List<string> transactionManagersInfo, List<string> leaseManagersInfo, Dictionary<int, List<string>> slotsDic, int slotNum)
        {
            this.nick = nick;
            this.idNum = idNum;
            this.slotNum = slotNum;
            this.slots = slotsDic;

            transactionManagers = new();

            foreach (string serverInfo in transactionManagersInfo)
            {
                string serverId = serverInfo.Split(" ")[1];
                string serverUrl = serverInfo.Split(" ")[3];
                GrpcChannel channel = GrpcChannel.ForAddress(serverUrl);

                transactionManagers.Add(serverId, new TransactionManagerInnerService.TransactionManagerInnerServiceClient(channel));
            }

            leaseManagers = new();

            foreach (string serverInfo in leaseManagersInfo)
            {
                string serverId = serverInfo.Split(" ")[1];
                string serverUrl = serverInfo.Split(" ")[3];
                GrpcChannel channel = GrpcChannel.ForAddress(serverUrl);

                leaseManagers.Add(serverId, new PaxosManagerService.PaxosManagerServiceClient(channel));
            }

            Task.Run(() => {

                // Sleep for an entire timeslot
                FileUtils.PrintAndFillLine(" Slot: 1 ", '_');

                Thread.Sleep(slotTime);

                // Every slot, start a new slot
                Task.Run(() =>
                {
                    while (epoch < slotNum)
                    {
                        FileUtils.PrintAndFillLine($" Slot: {++epoch} ", '_');

                        Thread.Sleep(slotTime);
                    }
                });

            });
        }

        // Depois de um pedido do cliente, o TxM valida se já tem a lease para realizar o pedido, caso tenha, não precisa de pedir a lease.
        private bool CheckValidLease(string txMID, List<string> dadIntArray)
        {
            foreach (var key in dadIntArray)
            {
                if (LeaseLog.TryGetValue(key, out var values) && values.Count > 0 && values[0].TxMID == txMID)
                {
                    return true;
                }
            }

            return false;
        }

        private bool CheckIfSecondInRow(string txMID, List<string> dadIntArray)
        {
            foreach (var key in dadIntArray)
            {
                if(LeaseLog.TryGetValue(key, out var values) && values[1].TxMID != txMID && values[0].TxMID != txMID)
                {
                    return false;
                }
            }

            return true;
        }

        private List<string> GetFirstRowTMs(List<string> dadIntArray)
        {
            List<string> firstRowIds = new List<string>();
            foreach (var key in dadIntArray)
            {
                if(LeaseLog.TryGetValue(key, out var values) && values.Count > 0)
                {
                    firstRowIds.Add(values[0].TxMID);
                }
            }
            return firstRowIds.Distinct().ToList();
        }

        // Por simplicidade podemos sempre executar esta função depois de realizar a transaction, independentemente de ser o unico TxM a ter a lease, a funçao so apaga caso exista
        // mais algum TxM na fila, podemos decidir se libertamos a lease dando o array de strings ou apenas o stamp da lease
        public void ReleaseLease(List<string> dadIntArray)
        {
            foreach (var key in dadIntArray)
            {
                if (LeaseLog.TryGetValue(key, out var values) && values.Count > 0)
                {
                    LeaseLog[key].RemoveAt(0);
                }
            }

            lock (LeaseLog)
            {
                Monitor.PulseAll(LeaseLog);
            }
        }

        // Quando o TxM pede a lease, o LM devolve sempre o seu pedido, e assim entra na fila para os diversos DadInts, depois à medida que as leases antigas vao sendo libertadas
        // as próximas da fila podem ser usadas
        private void AddLeaseToLog(List<string> dadIntArray, string nick)
        {
            foreach (var key in dadIntArray)
            {
                if (LeaseLog.TryGetValue(key, out var _))
                {
                    LeaseLog[key].Add((epoch, nick));
                } else
                {
                    LeaseLog.Add(key, new List<(int, string)> { (epoch, nick) });
                }
            }
        }

        public override Task<StatusTMReply> Status(StatusTMRequest request, ServerCallContext context)
        {   
            lock(this)
            {
                if (epoch > 0)
                {
                    string status = FileUtils.IsCrashed(idNum, slots[epoch]) ? "Crashed" : "Alive";
                    List<string> leases = new();

                    foreach (var lease in LeaseLog)
                    {
                        foreach (var lst in lease.Value)
                        {
                            if (lst.Item2 == nick)
                            {
                                leases.Add(lease.Key);
                            }
                        }
                    }

                    FileUtils.PrintAndFillLine(" Status ", '*');
                    FileUtils.PrintAndFillLine($" Server Name: {nick} ", ' ');
                    FileUtils.PrintAndFillLine($" Server ID: {idNum} ", ' ');
                    FileUtils.PrintAndFillLine($" Status: {status}", ' ');
                    FileUtils.PrintAndFillLine($" Suspects: {string.Join(',', FileUtils.GetOwnSuspects(slots[epoch], nick))}", ' ');
                    FileUtils.PrintAndFillLine($" Leases: {string.Join(',', leases)}", ' ');
                    FileUtils.PrintAndFillLine(" End ", '*');
                }

                return Task.FromResult(new StatusTMReply { });
            }
        }

        // Handle a new Transaction from a Client
        public override Task<TransactionReply> TransactionSubmit(TransactionRequest request, ServerCallContext context)
        {
            if (FileUtils.IsCrashed(idNum, slots[epoch]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{idNum} is Unavailable!"));
            }

            return Task.FromResult(HandleTransactionSubmit(request));
        }

        public TransactionReply HandleTransactionSubmit(TransactionRequest request)
        {
            Console.WriteLine($"New Transaction Request from '{request.Client}'");

            List<string> objectsIds = request.ToWriteList.Select(obj => obj.Key).Union(request.ToReadList).Distinct().ToList();

            bool validLease;

            lock (LeaseLog)
            {
                validLease = CheckValidLease(this.nick, objectsIds);    
            }

            if (!validLease)
            {
                Console.WriteLine("Getting a new Lease...");

                LeaseRequest leaseRequest = new LeaseRequest { Requester = nick };
                leaseRequest.Keys.Add(objectsIds);

                BroadcastLeaseRequest(leaseRequest);

                
                lock (LeaseLog)
                {
                    while (!CheckValidLease(this.nick, objectsIds))
                    {
                        //if (CheckIfSecondInRow(this.nick, objectsIds))
                        //{
                        //    List<string> txMIDsInFirst = GetFirstRowTMs(objectsIds);

                        //    foreach (var front in txMIDsInFirst)
                        //    {
                        //        Console.WriteLine("All First TXMs: " + front);

                        //        LeaseReleaseRequest req = new LeaseReleaseRequest { OwnerId = nick };
                        //        req.Keys.Add(objectsIds);

                        //        foreach (var tm in transactionManagers.Keys)
                        //        {
                        //            var _thread = new Thread(() =>
                        //            {
                        //                if (tm != nick) transactionManagers[tm].LeaseRelease(req);
                        //                Console.WriteLine("NICK = " + tm);
                        //            });

                        //            _thread.Start();
                        //        }
                        //    }
                        //    //start timer e mandar pedido para os txMIDsInFirst libertar
                        //}
                        //else
                        //{
                        //    Console.WriteLine("AEWFFFFFFFASEFASEFASEFASEF");
                        //}

                        Monitor.Wait(LeaseLog);
                    }
                }
            }
            else
            {
                Console.WriteLine("I already have the necessary lease!");
            }

            Console.WriteLine($"Lease ready to use!");

            TransactionReply transactionReply;

            lock (this)
            {
                BroadcastUpdate(request.ToWriteList.ToList(), objectsIds);
                // Ask TMs to release the already used lease
                LeaseReleaseRequest req = new LeaseReleaseRequest { OwnerId = nick, Slot = epoch };
                req.Keys.Add(objectsIds);

                foreach (var tm in transactionManagers.Keys)
                {
                    new Thread(() => 
                    { 
                        try
                        {
                            transactionManagers[tm].LeaseRelease(req);
                        }
                        catch (Exception)
                        {
                            Console.WriteLine($"Server '{tm}' failed to reply!");
                        }
                    }
                    ).Start();
                }

                // Reply to the cliente
                transactionReply = new TransactionReply();

                lock (dadInts) 
                {
                    foreach (var read in request.ToReadList)
                    {
                        DadInt dad = new DadInt { Key = read, Value = dadInts[read] };
                        transactionReply.ReplyList.Add(dad);
                    }

                    Console.WriteLine($"Replying to: {request.Client}\n");
                }
            }

            return transactionReply;
        }

        public override Task<Ack> LeasesAcquisition(LearnRequest request, ServerCallContext context)
        {
            if (FileUtils.IsCrashed(idNum, slots[epoch]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{idNum} is Unavailable!"));
            }

            return Task.FromResult(HandleLeasesAcquisition(request));
        }

        private Ack HandleLeasesAcquisition(LearnRequest request)
        {
            lock (this)
            {
                bool alreadyReceived;

                if (!slotHasValue.ContainsKey(request.Slot))
                {
                    alreadyReceived = false;
                }
                else 
                {
                    alreadyReceived = slotHasValue[request.Slot];
                }

                if (!alreadyReceived)
                {
                    foreach (Lease lease in request.Value)
                    {
                        AddLeaseToLog(lease.Keys.ToList(), lease.Requester);
                    }

                    slotHasValue.Add(request.Slot, true);

                    Console.WriteLine($"Slot '{request.Slot}': {request.Value}");
                }

                lock (LeaseLog) { Monitor.PulseAll(LeaseLog); }
            }

            return new Ack { };
        }

        public void BroadcastLeaseRequest(LeaseRequest request)
        {
            foreach (var recipient in leaseManagers.Keys)
            {
                Task.Run(() =>
                {
                    try
                    {
                        leaseManagers[recipient].RequestLease(request);
                    }

                    catch (Exception)
                    {
                        Console.WriteLine($"Server '{recipient}' failed to reply!");
                    }
                });
            }
        }

        private void BroadcastUpdate(List<DadInt> changes, List<string> keys)
        {
            int acceptedCounter = 0;
            int commitedCounter = 0;
            List<Thread> threads = new();

            foreach (string recipient in transactionManagers.Keys)
            {   
                Thread _thread = new Thread(() => 
                {
                    lock(dadInts)
                    {
                        var request = new UpdateRequest { UpdaterId = nick, TimeSlot = epoch };

                        try
                        {
                            var reply = transactionManagers[recipient].Update(request);

                            Console.WriteLine($"Transaction Manager '{reply.UpdatedId}' Accepted? = {reply.Accepted}");

                            if (reply.Accepted) 
                            { 
                                acceptedCounter++; 
                            }
                        }

                        catch (Exception)
                        {
                            Console.WriteLine($"Server '{recipient}' failed to reply!");
                        }

                        Monitor.Pulse(dadInts);
                    }
                });

                threads.Add(_thread);

                _thread.Start();
            }

            lock (dadInts)
            {
                while (acceptedCounter < (transactionManagers.Count + 1) / 2)
                {
                    Monitor.Wait(dadInts);
                }
            }

            Console.WriteLine($"Update accepted by {acceptedCounter} transaction managers!\n");
            Console.WriteLine($"Sending local commit instruction!\n");

            threads = new();

            foreach (string recipient in transactionManagers.Keys)
            {
                Thread _thread = new Thread(() => {

                    lock (dadInts)
                    {
                        var request = new CommitRequest { CommiterId = nick };
                        request.Objects.Add(changes);
                        request.Keys.Add(keys);

                        try
                        {
                            var reply = transactionManagers[recipient].Commit(request);

                            Console.WriteLine($"Transaction Manager '{reply.CommitedId}' Commited? = {reply.Commited}");

                            if (reply.Commited) { commitedCounter++; }
                        }

                        catch (Exception)
                        {
                            Console.WriteLine($"Server '{recipient}' failed to reply!");
                        }
                        
                        Monitor.Pulse(dadInts);
                    }
                });

                threads.Add(_thread);

                _thread.Start();
            }

            lock (dadInts)
            {
                while (commitedCounter < (transactionManagers.Count + 1) / 2)
                {
                    Monitor.Wait(dadInts);
                }

                foreach (var dad in changes)
                {
                    dadInts[dad.Key] = dad.Value;
                }
            }
        }
    }

}
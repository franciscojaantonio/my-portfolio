using Grpc.Core;
using Grpc.Net.Client;
using UtilsLibrary;
using Paxos.Logic;
using LeaseManager;

namespace Paxos.Services
{
    public class PaxosService : PaxosManagerService.PaxosManagerServiceBase
    {
        // Dictionary with List of Lease requests by epoch
        Dictionary<int, List<Lease>> requestedLeases = new();

        // Slots
        Dictionary<int, List<string>> slots = new();

        // About this node
        int id;
        int idx;
        string nick;
        int leader;

        // Paxos Roles
        Proposer proposer;
        Acceptor acceptor;
        Learner learner;

        // Paxos properties
        int epoch = 1;
        int majority;

        // Members of Paxos (Transaction Managers are extra learners)
        private Dictionary<string, PaxosManagerService.PaxosManagerServiceClient> lmServers = new();
        private Dictionary<string, TransactionManagerService.TransactionManagerServiceClient> tmServers = new();

        private Dictionary<int, string> lmServersMap = new();

        public PaxosService(int id, int idx, string nick, List<string> lmServersInfo, List<string> tmServersInfo, int slotTime, int slotNum, Dictionary<int, List<string>> slots)
        {
            this.id = id;
            this.idx = idx;
            this.nick = nick;
            this.leader = 1;

            this.slots = slots;
            
            foreach (string serverInfo in lmServersInfo)
            {
                string serverId = serverInfo.Split(" ")[1];
                string serverUrl = serverInfo.Split(" ")[3];

                lmServers.Add(serverId, new PaxosManagerService.PaxosManagerServiceClient(GrpcChannel.ForAddress(serverUrl)));

                lmServersMap.Add(lmServers.Count, serverId);
            }

            foreach (string serverInfo in tmServersInfo)
            {
                string serverId = serverInfo.Split(" ")[1];
                string serverUrl = serverInfo.Split(" ")[3];

                tmServers.Add(serverId, new TransactionManagerService.TransactionManagerServiceClient(GrpcChannel.ForAddress(serverUrl)));
            }

            this.proposer = new Proposer(id, lmServers);
            this.acceptor = new Acceptor(lmServers);
            this.learner  = new Learner(tmServers);

            this.majority = (lmServers.Count + 1) / 2;
           
            //Dictionary<int, List<string>> slotConfig = FileUtils.GetFlist();

            Task.Run(() => {

                // Sleep for an entire timeslot
                FileUtils.PrintAndFillLine(" Slot: 1 ", '_');

                Thread.Sleep(slotTime);

                // Every slot, start a new paxos round
                Task.Run(() =>
                {
                    while (epoch < slotNum)
                    {
                        FileUtils.PrintAndFillLine($" Slot: {++epoch} ", '_');

                        // Interrupt this thread after this timeslot has passed?
                        new Thread(() => { StartEpoch(epoch); }).Start();

                        Thread.Sleep(slotTime);
                    }
                });
            });
        }

        public override Task<StatusLMReply> Status(StatusLMRequest request, ServerCallContext context)
        {   
            lock(this) { 
                if (epoch > 0)
                {
                    string status = FileUtils.IsCrashed(idx, slots[epoch]) ? "Crashed" : "Alive";
                

                    FileUtils.PrintAndFillLine(" Status ", '*');
                    FileUtils.PrintAndFillLine($" Server Name: {nick} ", ' ');
                    FileUtils.PrintAndFillLine($" Server ID: {idx} ", ' ');
                    FileUtils.PrintAndFillLine($" Status: {status}", ' ');
                    FileUtils.PrintAndFillLine($" Suspects: {string.Join(',', FileUtils.GetOwnSuspects(slots[epoch], nick))}", ' ');
                
                    foreach (var l in learner.Learned)
                    {
                        FileUtils.PrintAndFillLine($" Leases for round: {l.Key} ", ' ');

                        foreach (Lease le in l.Value)
                        {
                            FileUtils.PrintAndFillLine($" Requester: {le.Requester} ", ' ');
                            FileUtils.PrintAndFillLine($" Objects: {string.Join(',', le.Keys)} ", ' ');
                        }
                    }
                    FileUtils.PrintAndFillLine(" End ", '*');
                }

                return Task.FromResult(new StatusLMReply { });
            }
        }

        private int FindProposerId(int num)
        {
            int proposerId = num;

            while (proposerId > lmServers.Count) 
            {
                proposerId -= lmServers.Count;
            }

            return proposerId;
        }

        private bool CheckForSuspicions(int epoch)
        {
            int currentLeader = FindProposerId(this.leader);
            
            List<string> suspects = FileUtils.GetOwnSuspects(slots[epoch], lmServersMap[id]);

            List<int> ids = new List<int> ( lmServersMap.Keys );
            
            List<int> serversBetween = new();

            if (currentLeader > id)
            {
                serversBetween.AddRange(ids.GetRange(0, id - 1));
                serversBetween.AddRange(ids.GetRange(currentLeader - 1, ids.Count - currentLeader + 1));
            }
            else if (currentLeader < id)
            {
                serversBetween.AddRange(ids.GetRange(currentLeader - 1, id - currentLeader));
            }

            Console.WriteLine($"Servers between: {string.Join(", ", serversBetween)} | Suspects: {string.Join(", ", suspects)}");

            foreach (var id in lmServersMap)
            {
                if (suspects.Contains(id.Value))
                {
                    serversBetween.Remove(id.Key);
                }
            }

            return serversBetween.Count == 0;
        }

        public void StartEpoch(int epoch)
        {
            if (this.leader != FindProposerId(proposer.Number)) 
            { 
                if (!CheckForSuspicions(epoch))
                {
                    return; 
                }
            };

            int requestPeriod = epoch - 1;

            List<Lease> requestsToPropose = new();

            lock (requestedLeases)
            {
                if (!requestedLeases.ContainsKey(requestPeriod))
                {
                    Console.WriteLine($"No requests for epoch '{epoch}'!");
                    return;
                }

                requestsToPropose = requestedLeases[requestPeriod];
            }

            // Check received Promises
            Dictionary<int, List<Lease>> pending = new();

            if (this.leader == FindProposerId(proposer.Number))
            {
                goto Phase2;
            }

            // Phase 1
            List<Promise> promises;

            lock (proposer)
            {
                promises = proposer.SendPrepare();
            }

            foreach (Promise promise in promises)
            {
                if (promise.Value.Count > 0)
                {
                    pending[promise.PromisedTo] = promise.Value.ToList();
                }
            }

        Phase2:
            List<Lease> valueToPropose;

            // Complete unfinished work
            if (pending.Count > 0)
            {
                // Talvez ordenar isto mas também dar append às requests que tem ? 
                valueToPropose = pending[pending.Keys.Max()];
            }

            // Send its value
            else
            {
                valueToPropose = requestsToPropose;
            }

            Console.WriteLine($"Value to propose on Accept:");

            foreach (Lease lease in valueToPropose)
            {
                Console.WriteLine($"  - {lease.Requester} -> {lease.Keys}");
            }

            // Phase 2
            List<Accepted> accepteds;

            lock (proposer)
            {
                accepteds = proposer.SendAccept(valueToPropose);
            }

            this.leader = FindProposerId(accepteds[0].AcceptedFrom);

            Console.WriteLine($"My leader is: {this.leader}");

            // Consensus Done (Remove from requests what has been delivered?) (Can we be sure that the learners know this value?)
            lock (requestedLeases)
            {
                requestedLeases.Remove(requestPeriod);
            }
        }

        public override Task<Ack> RequestLease(LeaseRequest request, ServerCallContext context)
        {
            if (FileUtils.IsCrashed(idx, slots[epoch]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{id} is Unavailable!"));
            }

            return Task.FromResult(HandleRequestLease(request));
        }

        public Ack HandleRequestLease(LeaseRequest request)
        {
            Console.WriteLine($"Got a new Lease Request from '{request.Requester}' for epoch '{epoch}'");

            Lease lease = new Lease { Requester = request.Requester };

            lease.Keys.Add(request.Keys);

            lock (requestedLeases)
            {
                if (requestedLeases.ContainsKey(epoch))
                {
                    requestedLeases[epoch].Add(lease);
                }

                else
                {
                    requestedLeases[epoch] = new List<Lease> { lease };
                }
            }

            return new Ack { };
        }

        public override Task<Promise> Prepare(PrepareRequest request, ServerCallContext context)
        {
            if (!FileUtils.CheckIfCanReply(idx, lmServersMap[id], lmServersMap[FindProposerId(request.Proposer)], slots[epoch]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{id} is Unavailable!"));
            }

            return Task.FromResult(HandlePrepare(request));
        }

        private Promise HandlePrepare(PrepareRequest request)
        {
            Console.WriteLine($"Received a Prepare from #{request.Proposer}");

            return acceptor.PrepareReceive(request);
        }

        public override Task<Accepted> Accept(AcceptRequest request, ServerCallContext context)
        {
            if (!FileUtils.CheckIfCanReply(idx, lmServersMap[id], lmServersMap[FindProposerId(request.Proposer)], slots[epoch]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{id} is Unavailable!"));
            }

            return Task.FromResult(HandleAccept(request));
        }

        private Accepted HandleAccept(AcceptRequest request)
        {
            Console.WriteLine($"Received an Accept from #{request.Proposer}");

            Accepted accepted = acceptor.AcceptReceive(request);

            if (accepted.IsAccepted)
            {
                this.leader = FindProposerId(accepted.AcceptedFrom);

                Console.WriteLine($"My leader is: {this.leader}");
            }

            return accepted;
        }

        public override Task<Ack> Learn(Accepted request, ServerCallContext context)
        {
            if (!FileUtils.CheckIfCanReply(idx, lmServersMap[id], lmServersMap[FindProposerId(request.AcceptedFrom)], slots[epoch]))
            {
                throw new RpcException(new Status(StatusCode.Unavailable, $"#{id} is Unavailable!"));
            }

            return Task.FromResult(HandleLearn(request));
        }

        private Ack HandleLearn(Accepted request)
        {
            return learner.Learn(epoch, request, majority);
        }
    }
}

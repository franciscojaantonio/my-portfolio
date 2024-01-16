using System.Threading.Channels;
using Grpc.Core;
using Grpc.Net.Client;
using LeaseManager;


namespace Paxos.Logic
{
    public class Proposer 
    {
        // Paxos properties
        bool isLeader;
        int proposerNum;

        // Paxos members communication
        Dictionary<string, PaxosManagerService.PaxosManagerServiceClient> servers;

        public int Number
        { 
            get { return proposerNum; } 
        }

        public Proposer(int proposerNum, Dictionary<string, PaxosManagerService.PaxosManagerServiceClient> servers)
        {
            this.servers = servers;
            this.proposerNum = proposerNum;
        }

        private void NextProposerId()
        {
            proposerNum += servers.Count;
        }

        public List<Promise> SendPrepare()
        {
            NextProposerId();

            Console.WriteLine($"Sending prepare with number: {proposerNum}");

            PrepareRequest request = new PrepareRequest { Proposer = proposerNum };

            List<Thread> threads = new();

            List<Promise> promises = new();

            foreach (var recipient in servers.Keys)
            {
                var _thread = new Thread(() =>
                {
                    lock (promises)
                    {
                        try
                        {
                            Promise promise = servers[recipient].Prepare(request);

                            if (!promise.IsPromised)
                            {
                                Console.WriteLine($"Prepare rejected for number: {request.Proposer}");
                                return;
                            }

                            Console.WriteLine($"Promise received for number: {promise.PromisedTo}");

                            promises.Add(promise);
                        }

                        catch (Exception)
                        {
                            Console.WriteLine($"Server '{recipient}' failed to reply!");
                        }
                            
                        Monitor.Pulse(promises);
                    }
                });

                threads.Add(_thread);

                _thread.Start();
            }
              
            lock (promises) 
            {
                while (promises.Count < ((servers.Count + 1) / 2))
                {
                    Monitor.Wait(promises);
                }
            }

            Console.WriteLine($"Got a majority ({promises.Count}) of promises!");

            return promises;
        }

        public List<Accepted> SendAccept(List<Lease> value)
        {
            Console.WriteLine($"Sending accept with number: {proposerNum}");

            AcceptRequest request = new AcceptRequest {  Proposer = proposerNum };

            request.Value.Add(value);

            List<Thread> threads = new();

            List<Accepted> accepteds = new();

            foreach (var recipient in servers.Keys)
            {
                var _thread = new Thread(() =>
                {
                    lock (accepteds)
                    {
                        try
                        {
                            Accepted accepted = servers[recipient].Accept(request);

                            if (!accepted.IsAccepted)
                            {
                                Console.WriteLine($"Accept rejected for number: {request.Proposer}");
                                return;
                            }

                            Console.WriteLine($"Accepted received for number: {accepted.AcceptedFrom}");

                            accepteds.Add(accepted);
                        }

                        catch (Exception)
                        {
                            Console.WriteLine($"Server '{recipient}' failed to reply!");
                        }

                        Monitor.Pulse(accepteds);
                    }
                });

                threads.Add(_thread);

                _thread.Start();
            }

            lock (accepteds)
            {
                while (accepteds.Count < ((servers.Count + 1) / 2))
                {
                    Monitor.Wait(accepteds);
                }
            }

            Console.WriteLine($"Got a majority ({accepteds.Count}) of accepteds!");

            return accepteds;
        }
    }

    public class Acceptor
    {
        // Paxos members communication
        Dictionary<string, PaxosManagerService.PaxosManagerServiceClient> servers;

        // Paxos properties
        int highestPrepare = 0;    // Biggest leader seen
        int highestAccept = 0;     // Biggest leader that I accepted
        List<Lease> value = new(); // Value accepted from acceptedFrom

        public Acceptor(Dictionary<string, PaxosManagerService.PaxosManagerServiceClient> servers)
        {
            this.servers = servers;
        }

        public Promise PrepareReceive(PrepareRequest request)
        {
            Promise reply;

            lock (this)
            {
                bool isPromised;

                if (request.Proposer > highestPrepare && request.Proposer > highestAccept)
                {
                    highestPrepare = request.Proposer;

                    isPromised = true;
                }

                else
                {
                    isPromised = false;
                }

                reply = new Promise { IsPromised = isPromised, PromisedTo = highestPrepare };

                reply.Value.Add(value);

            }

            return reply;
        }

        public Accepted AcceptReceive(AcceptRequest request)
        {
            Accepted reply;

            lock (this)
            {
                bool isAccepted;

                if (request.Proposer >= highestPrepare && request.Proposer >= highestAccept)
                {
                    highestAccept = request.Proposer;
                    value = request.Value.ToList();

                    isAccepted = true;
                }

                else
                {
                    isAccepted = false;
                }

                reply = new Accepted { IsAccepted = isAccepted, AcceptedFrom = highestAccept };

                reply.Value.Add(value);

                // Share the accepted value with learners if valid
                if (isAccepted)
                {
                    foreach (var recipient in servers.Keys)
                    {
                        Task.Run(() => {
                            try
                            {
                                servers[recipient].Learn(reply);
                            }

                            catch (Exception) { }
                        });
                    }
                }
            }
            return reply;
        }
    }

    public class Learner
    {
        // Transaction Managers members
        Dictionary<string, TransactionManagerService.TransactionManagerServiceClient> servers;

        // Paxos
        Dictionary<int, List<Lease>> learned = new();

        // Accepted counter
        Dictionary<int, int> counter = new();

        public Learner(Dictionary<string, TransactionManagerService.TransactionManagerServiceClient> servers) 
        {
            this.servers = servers;
        }

        public Ack Learn(int epoch, Accepted toLearn, int majority)
        {
            lock (this)
            {
                if (counter.ContainsKey(epoch))
                {
                    counter[epoch] += 1;
                }
                else { counter[epoch] = 1; }

                if (counter[epoch] >= majority)
                {
                    learned.Add(epoch, toLearn.Value.ToList());

                    Console.WriteLine($"New value learned:");
                    foreach (Lease v in toLearn.Value)
                    {
                        Console.WriteLine($"  - {v.Requester} -> {v.Keys}");
                    }

                    LearnRequest learnRequest = new LearnRequest { Slot = epoch };
                    learnRequest.Value.Add(toLearn.Value);

                    foreach (var recipient in servers.Keys)
                    {
                        Task.Run(() => {
                            try
                            {
                                servers[recipient].LeasesAcquisition(learnRequest);
                            }

                            catch (Exception)
                            {
                                Console.WriteLine($"Server '{recipient}' failed to reply!");
                            }
                        });
                    }
                }
            }

            return new Ack { };
        }
        
        public Dictionary<int, List<Lease>> Learned
        {
            get { return learned; }
        }
    }
}

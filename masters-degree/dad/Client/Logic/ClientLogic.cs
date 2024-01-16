using Grpc.Core;
using Grpc.Net.Client;

namespace Client.Logic
{
    public class ClientLogic
    {
        private string nick;

        private Dictionary<string, TransactionManagerService.TransactionManagerServiceClient> tmServers = new();
        private Dictionary<string, PaxosManagerService.PaxosManagerServiceClient> lmServers = new();

        public ClientLogic(string nick, List<string> tmServersInfo, List<string> lmServersInfo)
        {
            this.nick = nick;

            string serverId;
            string serverUrl;
            GrpcChannel channel;

            foreach (string serverInfo in tmServersInfo)
            {
                serverId = serverInfo.Split(" ")[1];
                serverUrl = serverInfo.Split(" ")[3];
                channel = GrpcChannel.ForAddress(serverUrl);

                tmServers.Add(serverId, new TransactionManagerService.TransactionManagerServiceClient(channel));
            }

            foreach (string serverInfo in lmServersInfo)
            {
                serverId = serverInfo.Split(" ")[1];
                serverUrl = serverInfo.Split(" ")[3];
                channel = GrpcChannel.ForAddress(serverUrl);

                lmServers.Add(serverId, new PaxosManagerService.PaxosManagerServiceClient(channel));
            }
        }

        public void GetStatus()
        {   
            foreach(string server in tmServers.Keys) 
            {   
                try
                {
                    tmServers[server].Status(new StatusTMRequest());
                }
                catch 
                {
                    Console.WriteLine($"Server '{server}' failed to reply!");
                }
            }

            foreach (string server in lmServers.Keys)
            {   
                try
                {
                    lmServers[server].Status(new StatusLMRequest());

                }
                catch 
                {
                    Console.WriteLine($"Server '{server}' failed to reply!");
                }
            }

        }

        public string SendTransaction(List<string> toRead, List<DadInt> toWrite)
        {
            string result = "Result:\n";

            var request = new TransactionRequest { Client = nick };
            request.ToReadList.Add(toRead);
            request.ToWriteList.Add(toWrite);

            Random rnd = new Random();
            List<string> keys = tmServers.Keys.ToList();
            int tm = rnd.Next(0, keys.Count);

            TransactionReply reply; ;
            
            try
            {
                reply = tmServers[keys[tm]].TransactionSubmit(request);
            }

            catch (Exception)
            {
                Console.WriteLine($"Server '{keys[tm]}' failed to reply!");
                result += " aborted";

                return result;
            }

            DadInt[] readResults = reply.ReadList.ToArray();

            for (int i = 0; i < readResults.Length; i++)
            {
                result += $"  {i}) {readResults[i]}\n";
            }

            result.Remove(result.LastIndexOf("\n"));

            return result;
        }
    }
}

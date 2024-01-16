using Grpc.Core;
using LeaseManager;
using Paxos.Services;
using UtilsLibrary;

class Program
{
    private static ManualResetEvent timerEvent = new ManualResetEvent(false);

    static void Main(string[] args)
    {
        int id;
        int idx;
        int port;
        int slotTime;
        int slotNum;
        (int, int, int) startTime;
        string nick;
        string hostname;
        string configPath;

        try
        {   
            if (args.Length > 12)
            {
                id = int.Parse(args[6]);
                slotTime = int.Parse(args[7]);
                configPath = args[4] + ' ' + args[5];
                startTime = (int.Parse(args[8]), int.Parse(args[9]), int.Parse(args[10]));
                slotNum = int.Parse(args[11]);
                idx = int.Parse(args[12]);
            }
            else
            {
                id = int.Parse(args[5]);
                configPath = args[4];
                slotTime = int.Parse(args[6]);
                startTime = (int.Parse(args[7]), int.Parse(args[8]), int.Parse(args[9]));
                slotNum = int.Parse(args[10]);
                idx = int.Parse(args[11]);
            }

            nick = args[1];
            string[] url = args[3].Split("//")[1].Split(":");
            hostname = url[0];
            port = int.Parse(url[1]);

            StartLeaseManagerServer(id, idx, nick, hostname, port, configPath, slotTime, startTime, slotNum);
        }

        catch (Exception e)
        {
            Console.WriteLine("Couldn't read the Server's arguments: " + e.Message);
            return;
        }
    }

    private static void StartLeaseManagerServer(int id, int idx, string nick, string hostname, int port, string configPath, int slotTime, (int, int, int) startTime, int slotNum)
    {
        Console.Title = $"[LM] {nick} #{id}";

        /*
         *  Starting the Lease Manager Server...
         */

        List<string> lmServersInfo = FileUtils.GetProcessesByType(configPath, "L");
        List<string> tmServersInfo = FileUtils.GetProcessesByType(configPath, "T");

        Dictionary<int, List<string>> slots = FileUtils.GetFlist(configPath);

        ServerPort serverPort = new ServerPort(hostname, port, ServerCredentials.Insecure);

        SetUpTimer(new TimeSpan(startTime.Item1, startTime.Item2, startTime.Item3));

        timerEvent.WaitOne();
        Console.WriteLine($"Server listening on port {serverPort.Port}\n");

        Server server = new Server
        {
            Services = {
                PaxosManagerService.BindService(new PaxosService(id, idx, nick, lmServersInfo, tmServersInfo, slotTime, slotNum, slots)),
            },
            Ports = { serverPort }
        };
        
        server.Start();

        Console.ReadLine();
    }

    private static void SetUpTimer(TimeSpan alertTime)
    {
        Timer timer;
        DateTime current = DateTime.Now;
        TimeSpan timeToGo = alertTime - current.TimeOfDay;

        if (timeToGo < TimeSpan.Zero)
        {
            Console.WriteLine("Given time has passed!");
            Console.ReadLine();
        }

        timer = new Timer(x =>
        {
            Console.WriteLine("Time reached, starting server...");
            timerEvent.Set();
        }, null, timeToGo, Timeout.InfiniteTimeSpan);
    }
}
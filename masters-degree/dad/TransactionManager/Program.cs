using Grpc.Core;
using TransactionManager;
using TransactionManager.Services;
using UtilsLibrary;

class Program
{
    private static ManualResetEvent timerEvent = new ManualResetEvent(false);

    static void Main(string[] args)
    {
        int id;
        int port;
        int index;
        int slotTime;
        int slotNum;
        (int, int, int) startTime;
        string nick;
        string hostname;
        string configPath;
        string rootPath = AppDomain.CurrentDomain.BaseDirectory.Split("ProcessManagement")[0];
        string configGlobal = "C:\\Users\\ist196909\\source\\repos\\dadtkv-project\\ProcessManagement\\ConfigFiles\\SampleConfig.txt";
        
        try
        {
            if (args.Length > 12)
            {
                id = int.Parse(args[6]);
                slotTime = int.Parse(args[7]);
                configPath = args[4] + ' ' + args[5];
                startTime = (int.Parse(args[8]), int.Parse(args[9]), int.Parse(args[10]));
                slotNum = int.Parse(args[11]);
                index = int.Parse(args[12]);
            }
            else
            {
                id = int.Parse(args[5]);
                configPath = args[4];
                slotTime = int.Parse(args[6]);
                startTime = (int.Parse(args[7]), int.Parse(args[8]), int.Parse(args[9]));
                slotNum = int.Parse(args[10]);
                index = int.Parse(args[11]);
            }

            nick = args[1];
            string[] url = args[3].Split("//")[1].Split(":");
            hostname = url[0];
            port = int.Parse(url[1]);

            configGlobal = configPath;
            StartTransactionManagerServer(nick, id, index, hostname, port, configPath, configGlobal, slotTime, startTime, slotNum);
        }

        catch (Exception e)
        {
            Console.WriteLine("Couldn't read the Server's arguments: " + e.Message);
            return;
        }
    }

    private static void StartTransactionManagerServer(string nick, int id, int index, string hostname, int port, string configPath, string configGlobal, int slotTime, (int, int, int) startTime, int slotNum)
    {
        Console.Title = $"[TM] {nick} #{id}";

        List<string> transactionManagersInfo = FileUtils.GetProcessesByType(configPath, "T");
        List<string> leaseManagersInfo = FileUtils.GetProcessesByType(configPath, "L");
        Dictionary<int, List<string>> dicList = FileUtils.GetFlist(configGlobal);

        ServerPort serverPort = new ServerPort(hostname, port, ServerCredentials.Insecure);

        SetUpTimer(new TimeSpan(startTime.Item1, startTime.Item2, startTime.Item3));
        timerEvent.WaitOne();
        Console.WriteLine($"Server listening on port {serverPort.Port}\n\n");

        // TM Service
        TransactionServerService tms = new TransactionServerService(id, nick, slotTime, transactionManagersInfo, leaseManagersInfo, dicList, slotNum);

        Server server = new Server
        {
            Services = {
                TransactionManagerService.BindService(tms),
                TransactionManagerInnerService.BindService(new TransactionServerInnerService(index, nick, dicList, tms)),
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
            //CHANGEME EXIT IF COMES TO HERE
            Console.ReadLine();
        }
        
        timer = new Timer(x =>
        {
            Console.WriteLine("Time reached, starting server...");
            timerEvent.Set();
        }, null, timeToGo, Timeout.InfiniteTimeSpan);
    }
}
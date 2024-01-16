using Client;
using Client.Logic;
using UtilsLibrary;

class Program
{
    private static ManualResetEvent timerEvent = new ManualResetEvent(false);
    
    static void Main(string[] args)
    {
        // Client properties
        string nick;
        (int, int, int) startTime;
        string configPath;
        string scriptPath;

        try
        {
            if (args.Length > 12)
            {
                configPath = args[4] + ' ' + args[5];
                startTime = (int.Parse(args[8]), int.Parse(args[9]), int.Parse(args[10]));
            }
            else
            {
                configPath = args[4];
                startTime = (int.Parse(args[7]), int.Parse(args[8]), int.Parse(args[9]));
            }

            nick = args[1];
            scriptPath = $"{AppDomain.CurrentDomain.BaseDirectory.Split("Client")[0]}/Client/Scripts/{args[3]}";
        }
        catch (Exception e)
        {
            Console.WriteLine("Couldn't read the client's arguments: " + e.Message);
            return;
        }

        Console.Title = $"[Client] {nick}";

        List<string> tmServersInfo = FileUtils.GetProcessesByType(configPath, "T");
        List<string> lmServersInfo = FileUtils.GetProcessesByType(configPath, "L");

        List<string> scriptCommands = FileUtils.GetClientScript(scriptPath);

        bool running = true;

        SetUpTimer(new TimeSpan(startTime.Item1, startTime.Item2, startTime.Item3));
        timerEvent.WaitOne();

        ClientLogic client = new ClientLogic(nick, tmServersInfo, lmServersInfo);

        Thread run = new(() =>
        {
            while (true)
            {
                foreach (string command in scriptCommands)
                {
                    if (running)
                    {
                        string[] values = command.Split(" ");

                        switch (values[0])
                        {
                            case "T":
                                Console.WriteLine($"Send transaction: ReadSet = {values[1]}, WriteSet = {values[2]}");

                                List<string> toRead = new();

                                try
                                {
                                    foreach (string value in values[1].Replace("(", "").Replace("\"", "").Replace(")", "").Split(","))
                                    {
                                        if (!string.IsNullOrEmpty(value))
                                        {
                                            toRead.Add(value);
                                        }
                                    }
                                }

                                catch (Exception) { }

                                List<DadInt> toWrite = new();

                                try
                                {
                                    foreach (string value in values[2].Replace("(", "").Replace("\"", "").Replace(")", "").Split(">,<"))
                                    {
                                        if (!string.IsNullOrEmpty(value))
                                        {
                                            string[] temp = value.Replace("<", "").Replace(">", "").Split(',');

                                            toWrite.Add(new DadInt { Key = temp[0], Value = int.Parse(temp[1]) });
                                        }
                                    }
                                }

                                catch (Exception) { }

                                string reply = client.SendTransaction(toRead, toWrite);

                                Console.WriteLine(reply);

                                break;

                            case "W":
                                Console.WriteLine($"[{nick}] Wait {values[1]} milliseconds");

                                Thread.Sleep(int.Parse(values[1]));

                                break;

                            case "S":
                                Console.WriteLine($"[{nick}] Status request");

                                client.GetStatus();

                                break;
                        }

                        Thread.Sleep(1000);
                    }

                    else
                    {
                        return;
                    }
                }
            }
        });
        run.Start();

        Console.ReadLine();

        running = false;
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



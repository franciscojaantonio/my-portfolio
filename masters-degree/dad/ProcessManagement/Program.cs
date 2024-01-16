/*
    All processes are launched and managed from here
*/
using System.Diagnostics;
using ProcessManagement.Logic;
using UtilsLibrary;

Console.Title = "Process Management";

/*
 *  Use 'configFile' to specify which config file to use
 */
string configFile = "config-3";

// Executables path
string rootPath = AppDomain.CurrentDomain.BaseDirectory.Split("ProcessManagement")[0];

string clientPath = $"{rootPath}/Client/bin/Debug/net6.0/Client.exe";
string transactionManagerPath = $"{rootPath}/TransactionManager/bin/Debug/net6.0/TransactionManager.exe";
string leaseManagerPath = $"{rootPath}/LeaseManager/bin/Debug/net6.0/LeaseManager.exe";

// Extract data from the config file
string configPath = $"{rootPath}/ProcessManagement/ConfigFiles/{configFile}";

List<string> processesInfo = FileUtils.GetProcessesInfo(configPath);
int slotTime = FileUtils.GetSlotTime(configPath);
(int, int, int) startTime = FileUtils.GetStartTime(configPath);
int slotNum = FileUtils.GetNumberSlots(configPath);

//CHANGEME FOR DEBUGGING PURPOSES IS ALWAYS PLUS 5 SECONDS
DateTime currentDateTime = DateTime.Now;

DateTime newDateTime = currentDateTime.AddSeconds(5);
int hour = newDateTime.Hour;
int minute = newDateTime.Minute;
int second = newDateTime.Second;
startTime = (hour, minute, second);
// Process Management
ProcessManager manager = new(configPath, transactionManagerPath, leaseManagerPath, clientPath, processesInfo, slotTime, startTime, slotNum);

if (!manager.Start())
{
    Console.WriteLine("Some processes haven't been started! Please check your configuration file");
}

// Handle user input (to kill processes)
Console.WriteLine("\nPlease Enter:");
Console.WriteLine("  !k) To kill a specific process");
Console.WriteLine("  !q) To kill all processes");

while (true)
{
    Console.Write("\nYour option: ");
    string? input = Console.ReadLine();

    if (input == null)
        continue;

    switch (input)
    {
        case "!k":
            Console.Write("Process ID: ");
            string? processId = Console.ReadLine();

            if (!string.IsNullOrEmpty(processId))
            {
                if (manager.KillProcess(processId))
                {
                    Console.WriteLine("Done!");
                }
                else
                {
                    Console.WriteLine($"Failure! There is no process with id '{processId}'");
                }
            }
            break;

        case "!q":
            manager.KillAll();
            Console.WriteLine("All processes have been stopped!");

            Console.WriteLine("\nPress Enter to exit...");
            Console.ReadLine();
            return;

        default:
            Console.WriteLine($"Invalid option '{input}'");
            break;
    }
}
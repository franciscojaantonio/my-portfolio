using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection.Metadata;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ProcessManagement.Logic
{
    internal class ProcessManager
    {
        [DllImport("user32.dll")]
        public static extern bool SetWindowPos(IntPtr hWnd, IntPtr hWndInsertAfter, int X, int Y, int cx, int cy, uint uFlags);

        [DllImport("user32.dll")]
        public static extern IntPtr GetDesktopWindow();

        [DllImport("user32.dll")]
        public static extern IntPtr GetDC(IntPtr hwnd);

        [DllImport("gdi32.dll")]
        public static extern int GetDeviceCaps(IntPtr hdc, int nIndex);

        public const int DESKTOPHORZRES = 118;
        public const int DESKTOPVERTRES = 117;

        private Dictionary<string, Process> _processes = new();

        private string _configPath;

        private int transactionManagerId = 0;
        private int leaseManagerId = 0;
        private int clientId = 0;
        private int index = 0;

        public ProcessManager(string configPath, string transactionManagerPath, string leaseManagerPath, string clientPath, List<string> processesInfo, int slotTime, (int, int, int) startTime, int slotNum)
        {
            _configPath = configPath;
            GetFlistDebug(configPath);
            Console.WriteLine($"Loading {processesInfo.Count} Processes:");

            foreach (string processInfo in processesInfo)
            {
                switch (processInfo.Split(" ")[2])
                {
                    case "T":
                        Console.WriteLine($"  > Started a Transaction Manager [{processInfo.Split(" ")[1]}]");
                        SetUpProcess(transactionManagerPath, index++, GetNextId("T"), processInfo, slotTime, startTime, slotNum);
                        break;
                    case "L":
                        Console.WriteLine($"  > Started a Lease Manager [{processInfo.Split(" ")[1]}]");
                        SetUpProcess(leaseManagerPath, index++, GetNextId("L"), processInfo, slotTime, startTime, slotNum);
                        break;
                    case "C":
                        Console.WriteLine($"  > Started a Client [{processInfo.Split(" ")[1]}]");
                        SetUpProcess(clientPath, index++, GetNextId("C"), processInfo, slotTime, startTime, slotNum);
                        break;
                }
            }
        }

        public static Dictionary<int, List<string>> GetFlistDebug(string filePath)
        {
            Dictionary<int, List<string>> dictList = new Dictionary<int, List<string>>();
            string[] lines = File.ReadAllLines(filePath);
            foreach (string line in lines)
            {
                if (line.StartsWith("#"))
                    continue;
                string[] values = line.Split(" ");
                if (values[0].StartsWith("F"))
                {
                    int key = int.Parse(values[1]);
                    if (!dictList.ContainsKey(key))
                    {
                        dictList[key] = new List<string>(values.Skip(2));
                    }
                    else
                    {
                        dictList[key].AddRange(values.Skip(2));
                    }
                }
            }
            return dictList;
        }

        private void SetUpProcess(string path, int index, int id, string arguments, int slotTime, (int, int, int) startTime, int slotNum)
        {   
            string processId = arguments.Split(" ")[1];

            ProcessStartInfo startInfo = new ProcessStartInfo
            {
                FileName = path,
                UseShellExecute = true,
                Arguments = $"{arguments} {_configPath} {id} {slotTime} {startTime.Item1} {startTime.Item2} {startTime.Item3} {slotNum} {index}",
                CreateNoWindow = false,
            };
            _processes.Add(processId, new Process { StartInfo = startInfo });
        }

        private int GetNextId(string type)
        {
            int id = -1;

            if (type == "T")
                id = ++transactionManagerId;
            else if (type == "L")
                id = ++leaseManagerId;
            if (type == "C")
                return ++clientId;

            return id;
        }

        public bool Start()
        {
            bool status = true;
            foreach (Process process in _processes.Values)
            {
                bool aux = process.Start();

                if (status)
                    status = aux;
            }

            List<Process> terminals;

            while (true)
            {
                terminals = new();

                // Change according to your default terminal
                terminals.AddRange(Process.GetProcessesByName("TransactionManager"));
                terminals.AddRange(Process.GetProcessesByName("LeaseManager"));
                terminals.AddRange(Process.GetProcessesByName("Client"));
                terminals.AddRange(Process.GetProcessesByName("VsDebugConsole"));

                if (terminals.Count >= _processes.Count)
                {
                    break;
                }
            }

            ArrangeTerminals(terminals);

            return status;
        }

        // May not work well when the terminals have a minimum width
        private void ArrangeTerminals(List<Process> terminals)
        {
            IntPtr desktop = GetDesktopWindow();
            IntPtr dc = GetDC(desktop);

            int paddingX = 0;
            int paddingY = 0;

            // Screen Scale: 100% => 1, 125% => 0.75
            double factor = 1;

            int screenWidth = (int) ((GetDeviceCaps(dc, DESKTOPHORZRES) - paddingX) * factor);
            int screenHeight = (int) ((GetDeviceCaps(dc, DESKTOPVERTRES) - paddingY) * factor);

            int numTerminals = _processes.Count + 1;
            int numRows = 3;
            int numCols = (int) Math.Ceiling((double) numTerminals / numRows);

            int x = 0;
            int y = 0;

            if (numTerminals > 0)
            {
                int terminalWidth = screenWidth / numCols;
                int terminalHeight = screenHeight / numRows;

                foreach (Process terminal in terminals)
                {
                    IntPtr handle = terminal.MainWindowHandle;

                    if (handle != IntPtr.Zero)
                    {
                        SetWindowPos(handle, IntPtr.Zero, x * terminalWidth, y * terminalHeight, terminalWidth, terminalHeight, 0);

                        x++;

                        if (x == numCols)
                        {
                            x = 0;

                            if (y < numRows)
                                y++;                        
                        }
                    }
                }
            }

            Console.WriteLine($"  Terminals arranged in {numRows} rows!");
        }

        public bool KillProcess(string processId)
        {
            if (_processes.ContainsKey(processId))
            {
                _processes[processId].Kill();
                return _processes.Remove(processId);
            }

            return false;
        }

        public void KillAll()
        {
            foreach (Process process in _processes.Values)
            {
                process.Kill();
            }
        }
    }
}

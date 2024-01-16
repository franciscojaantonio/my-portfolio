using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace UtilsLibrary
{
    public static class FileUtils
    {
        public static List<string> GetProcessesByType(string filePath, string type)
        {
            List<string> info = new();
            try
            {   
                string[] lines = File.ReadAllLines(filePath);

                foreach (string line in lines)
                {
                    // Ignore comments
                    if (line.StartsWith("#"))
                        continue;

                    string[] values = line.Split(" ");

                    if (values[0].StartsWith("P") && values[2].StartsWith(type))
                    {
                        info.Add(line);
                    }
                }
            }

            catch (IOException e)
            {
                Console.WriteLine(e.Message);
            }

            return info;
        }

        // Get all processes info (TransactionManager, LeaseManager and Client)
        public static List<string> GetProcessesInfo(string filePath)
        {
            List<string> info = new();
            try
            {
                string[] lines = File.ReadAllLines(filePath);

                foreach (string line in lines)
                {
                    // Ignore comments
                    if (line.StartsWith("#"))
                        continue;

                    string[] values = line.Split(" ");

                    if (values[0].StartsWith("P"))
                    {
                        info.Add(line);
                    }
                }
            }

            catch (IOException e)
            {
                Console.WriteLine(e.Message);
            }

            return info;
        }

        public static (int, int, int) GetStartTime(string filePath)
        {
            int hour = 0;
            int minute = 0;
            int second = 0;

            try
            {
                string[] lines = File.ReadAllLines(filePath);

                foreach (string line in lines)
                {
                    if (line.StartsWith("#"))
                        continue;

                    string[] values = line.Split(" ");

                    if (values[0].StartsWith("T"))
                    {   
                        string[] time = values[1].Split(":");
                        hour = int.Parse(time[0]);
                        minute = int.Parse(time[1]);
                        second = int.Parse(time[2]);
                    }
                }
            }
            catch (IOException e)
            {
                Console.WriteLine(e.Message);
            }

            return (hour, minute, second);
        }

        public static int GetSlotTime(string filePath)
        {
            int time = 0;

            try
            {
                string[] lines = File.ReadAllLines(filePath);

                foreach (string line in lines)
                {
                    if (line.StartsWith("#"))
                        continue;

                    string[] values = line.Split(" ");

                    if (values[0].StartsWith("D"))
                    {
                        time = int.Parse(values[1]);
                    }
                }
            }
            catch(IOException e)
            {
                Console.WriteLine(e.Message);
            }

            return time;
        }
        
        public static int GetNumberSlots(string filePath)
        {
            int number = 0;

            try
            {
                string[] lines = File.ReadAllLines(filePath);

                foreach (string line in lines)
                {
                    if (line.StartsWith("#"))
                        continue;

                    string[] values = line.Split(" ");

                    if (values[0].StartsWith("S"))
                    {
                        number = int.Parse(values[1]);
                    }
                }
            }
            catch (IOException e)
            {
                Console.WriteLine(e.Message);
            }

            return number;
        }

        public static List<string> GetSystemBehavior(string filePath)
        {
            List<string> info = new();
            try
            {
                string[] lines = File.ReadAllLines(filePath);

                foreach (string line in lines)
                {
                    // Ignore comments
                    if (line.StartsWith("#"))
                        continue;

                    string[] values = line.Split(" ");

                    if (values[0].StartsWith("P"))
                        continue;

                    info.Add(line);
                }
            }

            catch (IOException e)
            {
                Console.WriteLine(e.Message);
            }

            return info;
        }

        public static List<string> GetClientScript(string filePath)
        {
            List<string> info = new();
            try
            {   
                string[] lines = File.ReadAllLines(filePath);

                foreach (string line in lines)
                {
                    // Ignore comments
                    if (line.StartsWith("#"))
                        continue;

                    info.Add(line);
                }
            }

            catch (IOException e)
            {
                Console.WriteLine(e.Message);
            }

            return info;
        }

        public static Dictionary<int, List<string>> GetFlist(string filePath)
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

        public static bool CheckIfCanReply(int serverOrder, string nick, string nickTo, List<string> line)
        {
            if (line[serverOrder].Equals("C")) return false;

            List<string> suspectPairs = GetSuspicions(line);

            foreach (var pair in suspectPairs)
            {
                String[] currentPair = pair.Split(',');
                if (currentPair[0].Contains(nick) && currentPair[1].Contains(nickTo)) return false;
            }

            return true;
        }

        public static List<string> GetSuspicions(List<string> line)
        {
            List<string> suspectPairs = new List<string>();

            foreach (var e in line)
            {
                if (e.Contains('('))
                {
                    suspectPairs.Add(e);
                }
            }

            return suspectPairs;
        }

        public static List<string> GetOwnSuspects(List<string> line, string nick)
        {
            List<string> ownSuspects = new List<string>();
            List<string> suspectPairs = GetSuspicions(line);
            foreach (string s in suspectPairs)
            {
                var pair = s.Split(',');
                if (pair[0].Contains(nick))
                {
                    ownSuspects.Add(pair[1].Remove(pair[1].Length - 1));
                }
            }
            return ownSuspects;
        }

        public static bool IsCrashed(int serverOrder, List<string> line)
        {
            return line[serverOrder].Equals("C");
        }

        public static void PrintAndFillLine(string msg, char fillWith)
        {
            int totalWidth = Console.WindowWidth;

            int leftPadding = (totalWidth - msg.Length) / 2;
            int rightPadding = totalWidth - msg.Length - leftPadding;

            string centeredText = new string(fillWith, leftPadding) + msg + new string(fillWith, rightPadding);

            Console.WriteLine(centeredText);
        }
    }
}
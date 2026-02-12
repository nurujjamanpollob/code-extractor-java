using System;
using System.Collections.Generic;
using System.Linq;

namespace Company.Project
{
    public class Processor
    {
        public string Name { get; set; }

        public Processor(string name)
        {
            Name = name;
        }

        public async System.Threading.Tasks.Task ProcessAsync(IEnumerable<string> items)
        {
            foreach (var item in items)
            {
                Console.WriteLine($"{Name} is processing {item}");
                await System.Threading.Tasks.Task.Delay(100);
            }
        }
    }

    class Program
    {
        static async System.Threading.Tasks.Task Main(string[] args)
        {
            var p = new Processor("MainProcessor");
            var list = new List<string> { "One", "Two", "Three" };
            await p.ProcessAsync(list);
        }
    }
}
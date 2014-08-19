// The Sandbox is designed to get you started with learning 
// about the HotSpot JIT compiler. It is not a substitute 
// for a well-designed benchmarking framework such as JMH.
// http://openjdk.java.net/projects/code-tools/jmh

public class SandboxTestLoad
{
    public static void main(String[] args)
    {
        SandboxTest test = new SandboxTest();

        int sum = 0;

        for (int i = 0 ; i < 1_000_000; i++)
        {
            sum = test.add(sum, 1);
        }

        System.out.println("Sum:" + sum);
    }
}
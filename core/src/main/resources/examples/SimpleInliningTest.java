// The Sandbox is designed to help you learn about the HotSpot JIT compilers.
// Please note that the JIT compilers may behave differently when isolating a method
// in the Sandbox compared to running your whole application.

public class SimpleInliningTest
{
    public SimpleInliningTest()
    {
        int sum = 0;

        // 1_000_000 is F4240 in hex
        for (int i = 0 ; i < 1_000_000; i++)
        {
            sum = this.add(sum, 99); // 63 hex
        }

        System.out.println("Sum:" + sum);
    }

    public int add(int a, int b)
    {
        return a + b;
    }

    public static void main(String[] args)
    {
        new SimpleInliningTest();
    }
}

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

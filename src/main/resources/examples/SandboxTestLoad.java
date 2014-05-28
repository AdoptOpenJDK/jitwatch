public class SandboxTestLoad
{
    private static final int ONE_MILLION = 1_000_000;

    public static void main(String[] args)
    {
        SandboxTest test = new SandboxTest();

        int sum = 0;

        for (int i = 0 ; i < ONE_MILLION; i++)
        {
            sum = test.add(sum, 1);
        }

        System.out.println("Sum:" + sum);
    }
}

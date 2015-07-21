public class LockCoarsen
{
    private java.util.Random random = new java.util.Random();

    public LockCoarsen()
    {
        long count = 0;

        // both calls to increment() should be inlined
        for (int i = 0; i < 20_000; i++)
        {
            synchronized(this)
            {
                count = increment(count);
            }

            count -= 5;

            synchronized(this)
            {
                count = increment(count);
            }
        }

        System.out.println(count);
    }

    public long increment(long input)
    {
        return input + random.nextInt(5);
    }

    public static void main(String[] args)
    {
        new LockCoarsen();
    }
}
public class LockTest
{
    public LockTest()
    {
        long count = 0;

        for (int i = 0; i < 100_000_000; i++)
        {
            synchronized(this)
            {
                count = increment(count);
            }
        }

        System.out.println(count);
    }

    public synchronized long increment(long input)
    {
        return input + 1;
    }

    public static void main(String[] args)
    {
        new LockTest();
    }
}
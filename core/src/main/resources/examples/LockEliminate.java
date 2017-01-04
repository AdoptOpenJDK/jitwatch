public class LockEliminate
{
    public LockEliminate()
    {
        long count = 0;

        for (int i = 0; i < 1_000_000; i++)
        {
            synchronized(this)
            {
                count = increment(count);
                count = decrement(count);
            }

            count = increment(count);
        }

        System.out.println(count);
    }

    private synchronized long increment(long input)
    {        
        return input + 0xDEAD;
    }

    private synchronized long decrement(long input)
    {        
        return input - 0xBEEF;
    }

    public static void main(String[] args)
    {
        new LockEliminate();
    }
}
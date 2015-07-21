public class LockEliminateViaInlining
{
    private java.util.Random random = new java.util.Random();

    public LockEliminateViaInlining()
    {
        long count = 0;

        for (int i = 0; i < 20_000; i++)
        {
            synchronized(this)
            {
                // these calls should inline
                // nested lock on same object eliminated?
                count = increment(count);
                count = decrement(count);
            }
        }

        System.out.println(count);
    }

    public synchronized long increment(long input)
    {        
        return input + random.nextInt(5);
    }

    public synchronized long decrement(long input)
    {        
        return input - random.nextInt(5);
    }

    public static void main(String[] args)
    {
        new LockEliminateViaInlining();
    }
}
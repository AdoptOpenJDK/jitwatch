public class LockCoarsen
{
    public LockCoarsen()
    {
        java.util.Random random = new java.util.Random();

        long sum = 0;
 
        Object lock = new Object();

        for (int i = 0; i < 1_000_000; i++)
        {
            synchronized(lock)
            {
                sum += random.nextLong();
            }

            synchronized(lock)
            {
                sum -= random.nextLong();
            }
        }

        System.out.println(sum);
    }

    public static void main(String[] args)
    {
        new LockCoarsen();
    }
}
public class InlineElimAlloc
{
    public long outer(int i)
    {
        long sum = 0;

        for (int j = 0; j < 20_000; j++)
        {
             sum += inner(i, j);
        }

         return sum;
    }

    public long inner(int i, int j)
    {
        long sum = 0;

        int[] parts = new int[2];

        parts[0] = i;
        parts[1] = j;

        java.util.Random random = new java.util.Random();

        if (random.nextBoolean())
        {
            sum += parts[0];
        }
        else
        {
            sum += parts[1];
        }

        return sum;
    }

    public static void main(final String[] args)
    {
        InlineElimAlloc test = new InlineElimAlloc();

        long sum = 0;

        for (int i = 0; i < 20_000; i++)
        {
            sum += test.outer(i);
        }

        System.out.println(sum);
    }
}
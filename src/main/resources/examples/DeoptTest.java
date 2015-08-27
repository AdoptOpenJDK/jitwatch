public class DeoptTest
{
    private long greater = 0;
    private long less = 0;
    private long direction = -1;

    private static final long MAX = 1_000_000;
    private static final long MIN = -MAX;

    public DeoptTest()
    {
        long current = MAX;
        long endTime = System.currentTimeMillis() + 20_000; // 20 seconds

        while (System.currentTimeMillis() < endTime)
        {
            checkZero(current);

            current += direction;

            if (current >= MAX || current <= MIN)
            {
                direction = -direction;
            }            
        }

        System.out.println(greater + " > 0, " + less + " < 0.");
    }

    private void checkZero(long value)
    {
        if (value > 0)
        {
            greater();
        }
        else
        {
            less();
        }
    }

    public void greater()
    {
        greater++;
    }

    public void less()
    {
        less++;
    }

    public static void main(String[] args)
    {
        new DeoptTest();
    }
}
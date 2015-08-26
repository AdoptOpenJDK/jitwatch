public class DeoptTest
{
    private long greater = 0;
    private long less = 0;

    private static final long START = 1_000_000;
    private static final long END = -1_000_000;

    public Deopt2()
    {
        long current = START;

        while (current > END)
        {
            checkZero(current);

            current--;
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

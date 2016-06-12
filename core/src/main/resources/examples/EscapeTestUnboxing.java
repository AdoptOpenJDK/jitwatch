import java.util.Vector;
import java.util.Random;

public class EscapeTestUnboxing
{
    private java.util.Random random = new java.util.Random();

    public EscapeTestUnboxing()
    {
        long sum = 0;

        for (int i = 0; i < 100_000; i++)
        {
            if (random.nextBoolean())
            {
                Integer asInteger = new Integer(i);
    
                sum += asInteger;
            }
        }

        System.out.println(sum);
    }

    public static void main(String[] args)
    {
        new EscapeTestUnboxing();
    }
}
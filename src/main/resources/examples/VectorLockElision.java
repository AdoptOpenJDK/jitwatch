import java.util.Vector;
import java.util.Random;

public class VectorLockElision
{
    private java.util.Random random = new java.util.Random();

    public VectorLockElision()
    {
        Vector<Integer> vector = new Vector<>();

        for (int i = 0; i < 100_000; i++)
        {
            if (random.nextBoolean())
            {
                Integer asInteger = new Integer(i);
    
                vector.add(asInteger);
            }
        }

        System.out.println(vector.size());
    }

    public static void main(String[] args)
    {
        new VectorLockElision();
    }
}
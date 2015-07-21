import java.util.Vector;

public class ElisionTest
{
    public ElisionTest()
    {
        Vector<Long> vector = new Vector<>();

        for (long i = 0; i < 100_000; i++)
        {
            vector.add(i); // add is synchronized
        }

        long sum = 0;

        for (int i = 0; i < vector.size(); i++)
        {
            sum += vector.get(i);
        }

        System.out.println("ElisionTest: " + sum);
    }

    public static void main(String[] args)
    {
        new ElisionTest();
    }
}
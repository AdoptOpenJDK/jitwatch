import java.util.Vector;

public class ElisionTest2
{
    public ElisionTest2()
    {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 100_000; i++)
        {
            buffer.append(i);
        }

        System.out.println("ElisionTest2: " + buffer.toString().length());
    }

    public static void main(String[] args)
    {
        new ElisionTest2();
    }
}
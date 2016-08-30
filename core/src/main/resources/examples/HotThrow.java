import java.util.Random;

public class HotThrow
{
    private Random random = new Random();

    public HotThrow()
    {
        StringBuilder builder = new StringBuilder();

        String string = "The quick brown fox jumps over the lazy dog";

        char[] chars = string.toCharArray();

        for (int i = 0 ; i < 1_000_000; i++)
        {
            int index = random.nextInt(100);

            char c = getChar(chars, index);

            builder.append(c);
        }

        System.out.println(builder.toString());
    }

    public char getChar(char[] chars, int index)
    {
        try
        {
            return chars[index];
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            return '*';
        }
    }

    public static void main(String[] args)
    {
        new HotThrow();
    }
}
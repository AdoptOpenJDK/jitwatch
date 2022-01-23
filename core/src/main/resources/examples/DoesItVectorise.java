public class DoesItVectorise
{
    public DoesItVectorise()
    {
        int[] array = new int[1024];

        for (int i = 0; i < 1_000_000; i++)
        {
            incrementArray(array, 1);
        }

        for (int i = 0; i < array.length; i++)
        {
            System.out.println(array[i]);
        }
    }

    public void incrementArray(int[] array, int constant)
    {
        int length = array.length;

        for (int i = 0; i < length; i++)
        {
            array[i] += constant;
        }
    }

    public static void main(String[] args)
    {
        new DoesItVectorise();
    }
}
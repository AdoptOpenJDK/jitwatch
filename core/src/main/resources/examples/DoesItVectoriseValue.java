public class DoesItVectoriseValue
{
    public DoesItVectoriseValue()
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

        for (Cursor c = Cursor.of(length); c.canAdvance(); c = c.advance())
        {
            array[c.position] += constant;
        }
    }

    public value record Cursor(int position, int length)
    {
        public Cursor {
            if (length < 0 || position > length)
            {
                throw new IllegalArgumentException();
            }
        }

        public static Cursor of(int length)
        {
            return new Cursor(0, length);
        }

        public boolean canAdvance()
        {
            return position < length;
        }

        public Cursor advance()
        {
            return new Cursor(position + 1, length);
        }
    }

    public static void main(String[] args)
    {
        new DoesItVectoriseValue();
    }
}

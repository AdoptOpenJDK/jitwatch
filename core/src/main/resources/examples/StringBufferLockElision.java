public class StringBufferLockElision
{
    public StringBufferLockElision()
    {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 500_000; i++)
        {
            String joined = concatPieces("a", "b", "c");

            buffer.append(joined);
        }

        System.out.println(buffer.toString());
    }

    private String concatPieces(String one, String two, String three)
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(one);
        buffer.append(two);
        buffer.append(three);

        return buffer.toString();
    }

    public static void main(String[] args)
    {
        new StringBufferLockElision();
    }
}
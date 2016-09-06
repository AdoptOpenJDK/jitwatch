public class TrivialMethod
{
    private int count;

    public TrivialMethod()
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 0 ; i < 1_000_000; i++)
        {
            incCount();

            int localCount = getCount();

            builder.append(localCount).append("\n");
        }

        System.out.println(builder.toString());
    }

    public int getCount()
    {
        return count;
    }

    public void incCount()
    {
        count++;
    }
   
    public static void main(String[] args)
    {
        new TrivialMethod();
    }
}
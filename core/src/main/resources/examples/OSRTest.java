public class OSRTest
{
  public OSRTest()
  {
    long sum = 0;

    java.util.Random r = new java.util.Random();

    for (int i = 0; i < 100_000; i++)
    {
      sum += r.nextInt();
    }

    for (int i = 0; i < 100_000; i++)
    {
      sum -= r.nextInt();
    }

    for (int i = 0; i < 100_000; i++)
    {
      sum *= r.nextInt();
    }

    for (int i = 0; i < 100_000; i++)
    {
      sum /= r.nextInt();
    }

    System.out.println(sum);
  }

  public static void main(String[] args)
  {
    new OSRTest();
  }
}
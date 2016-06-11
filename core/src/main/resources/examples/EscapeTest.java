// -XX:+DoEscapeAnalysis
// -XX:-DoEscapeAnalysis

public class EscapeTest
{
    public class Wrapper1 {
      private int value;
      public Wrapper1(int value) { this.value = value; }
      public int getValue() { return this.value; }
      public boolean equals(Wrapper2 wrapper2) { return this.value == wrapper2.getValue(); }
    }

    public class Wrapper2 {
      private int value;
      public Wrapper2(int value) { this.value = value; }
      public int getValue() { return this.value; }
    }

    private java.util.Random random = new java.util.Random();

    public String run()
    {
        String result;

        int matchYes = 0;
        int matchNo = 0;

        for (int i = 0; i < 100_000_000; i++)
        {
            int v1 = 0xABCD;
            int v2 = 0;

            if (random.nextBoolean())
            {
                v2 = 0xABCD;
            }

            final Wrapper1 wrapper1 = new Wrapper1(v1);
            final Wrapper2 wrapper2 = new Wrapper2(v2);

            // wrapper2 is NoEscape if inlining equals() succeeds
            // wrapper2 is ArgEscape if inlining equals() fails or disabled
            if (wrapper1.equals(wrapper2))
            {
                matchYes++;
            }
            else
            {
                matchNo++;
            }
        }

        result = matchYes + "/" + matchNo;

        return result;
    }

    public static void main(final String[] args)
    {
        String result = new EscapeTest().run();

        System.out.println(result);
    }
}
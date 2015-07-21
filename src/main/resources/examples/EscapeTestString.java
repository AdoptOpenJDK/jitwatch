public class EscapeTestString
{
    public class Wrapper1 {
      private String value;
      public Wrapper1(String value) { this.value = value; }
      public String getValue() { return this.value; }
      public boolean equals(Wrapper2 wrapper2) { return this.value.equals(wrapper2.getValue()); }
    }

    public class Wrapper2 {
      private String value;
      public Wrapper2(String value) { this.value = value; }
      public String getValue() { return this.value; }
    }

    private java.util.Random random = new java.util.Random();

    public String run()
    {
        String result;

        int matchYes = 0;
        int matchNo = 0;

        for (int i = 0; i < 100_000_000; i++)
        {
            String v1 = "0xABCD";
            String v2 = "0";

            if (random.nextBoolean())
            {
                v2 = "0xABCD";
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
        String result = new EscapeTestString().run();

        System.out.println(result);
    }
}
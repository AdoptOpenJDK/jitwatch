public class EscapeTestMultipleFields
{
    public class Wrapper1 {
        private int v1;
        private int v2;
        private int v3;
        public Wrapper1(int value) { v1 = value; v2 = v1+1; v3 = v2+1; }
        public int getValue() { return v1 + v2 + v3; }
        public boolean equals(Wrapper2 wrapper2) { return getValue() == wrapper2.getValue(); }
    }

    public class Wrapper2 {
        private int v1;
        private int v2;
        private int v3;
        public Wrapper2(int value) { v1 = value; v2 = v1+1; v3 = v2+1; }
        public int getValue() { return v1 + v2 + v3; }
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

            // prevent the clever VM jumping straight to the answer
            if (random.nextBoolean())
            {
                v2 = 0xABCD;
            }

            final Wrapper1 wrapper1 = new Wrapper1(v1);
            final Wrapper2 wrapper2 = new Wrapper2(v2);

            // wrapper2 is NoEscape if inlining of equals() succeeds
            // wrapper2 is ArgEscape if inlining fails or disabled
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
        String result = new EscapeTestMultipleFields().run();

        System.out.println(result);
    }
}
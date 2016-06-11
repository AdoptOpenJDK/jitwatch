public class EscapeTestManyFields
{
    public class Wrapper1 {
        private int v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11,v12,v13,v14,v15,v16,v17,v18,v19,v20;
        public Wrapper1(int value) { v1=value; v2=v1+1; v3=v2+1; v4=v3+1; v5=v4+1; 
                                     v6=v5+1; v7=v6+1; v8=v7+1; v9=v8+1; v10=v9+1;
                                     v11=v10+1; v12=v11+1; v13=v12+1; v14=v13+1; v15=v14+1; 
                                     v16=v15+1; v17=v16+1; v18=v17+1; v19=v18+1; v20=v19+1;}
        public int getValue() { return v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16 + v17 + v18 + v19 + v20; }
        public boolean equals(Wrapper2 wrapper2) { return getValue() == wrapper2.getValue(); }
    }

    public class Wrapper2 {
       private int v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11,v12,v13,v14,v15,v16,v17,v18,v19,v20;
       public Wrapper2(int value) { v1=value; v2=v1+1; v3=v2+1; v4=v3+1; v5=v4+1; 
                                    v6=v5+1; v7=v6+1; v8=v7+1; v9=v8+1; v10=v9+1;
                                    v11=v10+1; v12=v11+1; v13=v12+1; v14=v13+1; v15=v14+1; 
                                    v16=v15+1; v17=v16+1; v18=v17+1; v19=v18+1; v20=v19+1;}
       public int getValue() { return v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16 + v17 + v18 + v19 + v20; }
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
        String result = new EscapeTestManyFields().run();

        System.out.println(result);
    }
}
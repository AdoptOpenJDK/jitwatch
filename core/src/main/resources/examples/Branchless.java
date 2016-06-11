// Branchless code examples by Nathan Tippy (@NathanTippy)
public class Branchless
{        
    public Branchless()
    {
        int result1a = 0;
        int result1b = 0;
        int result2a = 0;
        int result2b = 0;

        for (int i = 0; i < 1_000_000; i++)
        {
            result1a = xEqualsYReturnAElseB(i, i, 98, 99);
            result1b = xEqualsYReturnAElseBVanilla(i, i, 98, 99);
            result2a = aLessThanZeroReturnBElseA(i, i);
            result2b = aLessThanZeroReturnBElseAVanilla(i, i);
        }

        System.out.println(result1a);
        System.out.println(result1b);
        System.out.println(result2a);
        System.out.println(result2b);
    }

    // return x == y ? a : b
    public int xEqualsYReturnAElseB(int x, int y, int a, int b)
    {
        int tmp = ((x - y) - 1) >> 31;

        int mask = (((x - y) >> 31) ^ tmp) & tmp;

        return (a & mask) | (b & (~mask));
    }

    // return x == y ? a : b
    public int xEqualsYReturnAElseBVanilla(int x, int y, int a, int b)
    {
        if (x == y)
        {
            return a;
        }
        else
        {
            return b;
        }
    }

    // return a < 0 ? b : a; 
    public int aLessThanZeroReturnBElseA(int a, int b)
    {
        int mask = a >> 31; 
        
        return (b & mask) | ((~mask) & a);
    }

    // return a < 0 ? b : a; 
    public int aLessThanZeroReturnBElseAVanilla(int a, int b)
    {
        if (a < 0)
        {
            return b;
        }
        else
        {
            return a;
        }
    }

  public static void main(String[] args)
  {
    new Branchless();
  }
}
// Branchless code examples by Nathan Tippy (@NathanTippy)
public class Branchless
{        
    public Branchless()
    {
        int result1 = 0;
        int result2 = 0;

        for (int i = 0; i < 1_000_000; i++)
        {
            result1 = xEqualsYReturnAElseB(i, i, 98, 99);
            result2 = aLessThanZeroReturnBElseA(i, i);
        }

        System.out.println(result1);
        System.out.println(result2);
    }

    // return x == y ? a : b
    public int xEqualsYReturnAElseB(int x, int y, int a, int b)
    {
        int tmp = ((x - y) - 1) >> 31;

        int mask = (((x - y) >> 31) ^ tmp) & tmp;

        return (a & mask) | (b & (~mask));
    }

    // return a < 0 ? b : a; 
    public int aLessThanZeroReturnBElseA(int a, int b)
    {
        int mask = a >> 31; 
        
        return (b & mask) | ((~mask) & a);
    }

  public static void main(String[] args)
  {
    new Branchless();
  }
}
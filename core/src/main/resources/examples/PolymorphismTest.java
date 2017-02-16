public class PolymorphismTest
{
  public interface Coin
  {
    void deposit();
  }

  public static int moneyBox = 0;

  public class Nickel implements Coin
  {
    public void deposit()
    {
      moneyBox += 5;
    }
  }

  public class Dime implements Coin
  {
    public void deposit()
    {
      moneyBox += 10;
    }
  }

  public class Quarter implements Coin
  {
    public void deposit()
    {
      moneyBox += 25;
    }
  }

  public PolymorphismTest()
  {
    Coin nickel = new Nickel();
    Coin dime = new Dime();
    Coin quarter = new Quarter();

    Coin coin = null;

    // change the variable maxImplementations to control the inlining behaviour
    // 2 = bimorphic dispatch   - the method call will be inlined
    // 3 = megamorphic dispatch - the method call will not be inlined

    final int maxImplementations = 2;

    for (int i = 0; i < 1_000_000; i++)
    {
       switch(i % maxImplementations)
       {
         case 0: coin = nickel; break;
         case 1: coin = dime; break;
         case 2: coin = quarter; break;
       }

       coin.deposit();
    }

    System.out.println("moneyBox:" + moneyBox);
  }

  public static void main(String[] args)
  {
    new PolymorphismTest();
  }
}

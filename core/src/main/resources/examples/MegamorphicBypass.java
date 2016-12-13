public class MegamorphicBypass
{
  public interface Coin { void deposit(); }

  public class Nickel implements Coin { public void deposit() { moneyBox += 5; } }

  public class Dime implements Coin { public void deposit() { moneyBox += 10; } }

  public class Quarter implements Coin { public void deposit() { moneyBox += 25; } }

  public static int moneyBox = 0;

  public MegamorphicBypass()
  {
    Coin nickel = new Nickel();
    Coin dime = new Dime();
    Coin quarter = new Quarter();

    Coin coin = null;

    final int maxImplementations = 3;

    for (int i = 0; i < 1_000_000; i++)
    {
       switch(i % maxImplementations)
       {
         case 0: coin = nickel; break;
         case 1: coin = dime; break;
         case 2: coin = quarter; break;
       }

       // peel off one type with an instanceof check
       if (coin instanceof Nickel)
       {
         coin.deposit();
       }
       else
       {
         // this callsite will now only observe 2 types and can use bimorphic inlining
         coin.deposit();
       }
    }

    System.out.println("moneyBox:" + moneyBox);
  }

  public static void main(String[] args) {
    new MegamorphicBypass();
  }
}
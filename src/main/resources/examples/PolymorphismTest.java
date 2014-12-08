public class PolymorphismTest
{
  public interface Animal
  {
    void speak();
  }

  public class Dog implements Animal
  {
    public int woofs = 0;
    public void speak() { woofs++; }
  }

  public class Cat implements Animal
  {
    public int miaows = 0;
    public void speak() { miaows++; }
  }

  public class Cow implements Animal
  {
    public int moos = 0;
    public void speak() { moos++; }
  }

  public PolymorphismTest()
  {
    Animal dog = new Dog();
    Animal cat = new Cat();
    Animal cow = new Cow();

    Animal creature = null;

    // Run with -XX:-TieredCompilation and -XX:-Inline
    // to see the effect of HotSpot optimising the virtual call

    // 1 = monomorphic dispatch - virtual call will be optimised
    // 2 = bimorphic dispatch   - virtual call will be optimised
    // 3 = polymorphic dispatch - virtual call will not be optimised

    final int maxImplementations = 2;

    for (int i = 0; i < 100000; i++)
    {
       switch(i % maxImplementations)
       {
         case 0: creature = dog; break;
         case 1: creature = cat; break;
         case 2: creature = cow; break;
       }

       creature.speak();
    }
  }

  public static void main(String[] args)
  {
    new PolymorphismTest();
  }

}
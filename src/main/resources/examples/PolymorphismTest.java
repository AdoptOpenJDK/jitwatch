public class PolymorphismTest
{
  public interface Animal
  {
    void speak();
  }

  public static int woofs = 0, miaows = 0, moos = 0;

  public class Dog implements Animal
  {
    public void speak() { woofs++; }
  }

  public class Cat implements Animal
  {
    public void speak() { miaows++; }
  }

  public class Cow implements Animal
  {
    public void speak() { moos++; }
  }

  public PolymorphismTest()
  {
    Animal dog = new Dog();
    Animal cat = new Cat();
    Animal cow = new Cow();

    Animal creature = null;

    // change the variable maxImplementations to control the inlining behaviour
    // 2 = bimorphic dispatch   - the method call will be inlined
    // 3 = polymorphic dispatch - the method call will not be inlined

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

    System.out.println("Woofs:" + woofs + " Miaows:" + miaows + " Moos:" + moos);
  }

  public static void main(String[] args)
  {
    new PolymorphismTest();
  }
}
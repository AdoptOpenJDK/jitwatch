public class PolymorphismTest
{
  public interface Animal
  {
    void speak();
  }

  public class Dog implements Animal
  {
    public void speak() { System.out.println("Woof!"); }
  }

  public class Cat implements Animal
  {
    public void speak() { System.out.println("Miaow!"); }
  }

  public class Cow implements Animal
  {
    public void speak() { System.out.println("Moo!"); }
  }

  public PolymorphismTest()
  {

    Animal dog = new Dog();
    Animal cat = new Cat();
    Animal cow = new Cow();

    Animal creature = null;

    for (int i = 0; i < 100000; i++)
    {
//       switch(i % 2) // bimorphic - will inline
       switch(i % 3) // polymorphic - won't inline
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
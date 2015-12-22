public class SafePointTest
{
    // Safepoints should appear at:
    // (1) back branch of uncounted loops
    // (2) entry to non-inlined method
    // (3) exit of non-inlined method

    private long count = 0;
    private long anotherCounter = 0;

    private long endTime = System.currentTimeMillis() + 5_000;
  
    public SafePointTest()
    {
        while (System.currentTimeMillis() < endTime)
        {
            count+= 0xFEED;
        }

        System.out.println("I counted to " + count);

        endTime += 5_000;

        while (System.currentTimeMillis() < endTime)
        {
            incCounter();
        }

        System.out.println("I counted to " + anotherCounter);
    }

    // -XX:CompileCommand=dontinline,SafePointTest.incCounter
    public void incCounter()
    {
        anotherCounter += 0xBEEF;
    }

    public static void main(String[] args)
    {
        new SafePointTest();
    }
}
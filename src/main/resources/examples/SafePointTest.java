public class SafePointTest
{
// todo - line number table popup from TriView
// todo - line numbers broken here

    private long count = 0;
    private long anotherCounter = 0;

    private long endTime = System.currentTimeMillis() + 10_000;
  
    public SafePointTest()
    {
        while (System.currentTimeMillis() < endTime)
        {
            count++;
        } // safepoint on back branch of uncounted loop

        endTime += 10_000;

        while (System.currentTimeMillis() < endTime)
        {
            incCounter();
        }

        System.out.println("I counted to " + count + " anotherCounter: " + anotherCounter);
    }

    // -XX:CompileCommand=dontinline,SafePointTest.incCounter
    public void incCounter()
    {
        anotherCounter +=1;
    }

    public static void main(String[] args)
    {
        new SafePointTest();
    }
}
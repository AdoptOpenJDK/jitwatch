public class IntrinsicTest
{
    public IntrinsicTest(int iterations)
    {
        long dstSum = 0;
        int[] src = new int[] { 1, 2, 3, 4, 5 };
        int[] dst = new int[src.length];

        for (int i = 0; i < iterations; i++)
        {
            // x86 has intrinsic for System.arrayCopy
            System.arraycopy(src, 0, dst, 0, src.length);

            for (int dstVal : dst)
            {
                dstSum += add(dstSum, dstVal);
            }
        }

        System.out.println("intrinsicTest: " + dstSum);
    }

    private long add(long a, long b)
    {
        return a + b;
    }

    public static void main(String[] args)
    {
        new IntrinsicTest(100_000);
    }
}
/*
Default values for InlineSmallCode are 1000 bytes (non-Tiered) and 2000 bytes (Tiered)

Method logPoint() will compile to more than 2000 bytes of native code on x86_64 OSX JDK8u121
due to the inlining of the StringBuilder.append() methods it contains.

This will make it too big to inline it into sumOfSquares() therefore the new Point object
will escape into logPoint and won't benefit from an eliminated allocation.

Running with -XX:InlineSmallCode=10000 will ensure logPoint() is inlined and sumOfSquares will not allocate
the new Point object on the heap.

It's a tradeoff increasing InlineSmallCode as more inlining will fill the code cache more quickly for the benefit of more
inlining-based optimisations.
*/

public class InlineSmallCode
{
    private class Point
    {
        private int x, y;
        public Point(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
    }

    public InlineSmallCode()
    {
        System.out.println(sumOfSquares(200_000));
    }

    private long sumOfSquares(int limit)
    {
        long sum = 0;

        for (int i = 0; i < limit; i++)
        {            
            Point p = new Point(i, i);

            // if logPoint has been compiled to native code that is larger than InlineSmallCode
            // then it won't be inlined and Point p will escape this method
            // and not benefit from escape analysis eliminated allocation

            logPoint(p);

            sum += p.getX() * p.getY();
        }

        return sum;
    }

    private void logPoint(Point p)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("Point(").append(p.getX()).append(",").append(p.getY()).append(")");

        System.out.println(builder.toString());
    }

    public static void main(String[] args)
    {
        new InlineSmallCode();
    }
}
public class PartialEscapeFail
{
    public static class EscapeConsumer {
        public int smallEnoughToInline(Integer integer) { // 325 bytes of bytecode (inlining limit is 325)
            int i = integer.intValue();
            return ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + 1;
        }

        public int tooBigToInline(Integer integer) { // 329 bytes of bytecode (inlining limit is 325)
            int i = integer.intValue();
            return ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + ((i + 3) >> 2) + ((i + 4) >> 2) + ((i + 5) >> 2) + ((i + 6) >> 2) + ((i + 7) >> 2) + ((i + 8) >> 2) +
                   ((i + 1) >> 2) + ((i + 2) >> 2) + 1 + 999; // javac is not an optimising compiler ;)
        }
    }

    public String run()
    {
        long result = 0;

        EscapeConsumer consumer = new EscapeConsumer();

        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 100_000_000; i++)
        {
            Integer integer = new Integer(i);

            if (random.nextBoolean())
            {
                result += consumer.smallEnoughToInline(integer);
            }
            else
            {
                // if you get an ArgEscape on any path you're busted
                result += consumer.tooBigToInline(integer);
            }
        }

        return "Result: " + result;
    }

    public static void main(final String[] args)
    {
        System.out.println(new PartialEscapeFail().run());
    }
}
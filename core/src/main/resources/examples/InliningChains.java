public class InliningChains
{
    public static void main(String[] args)
    {
        new InliningChains();
    }

    public InliningChains()
    {
        long count = 0;

        for (int i = 0; i < 100_000; i++)
        {
            count = chainA1(count);
            count = chainB1(count);
        }

        System.out.println("InliningChains: " + count);
    }

    private long chainA1(long count)
    {
        return 1 + chainA2(count);
    }

    private long chainA2(long count)
    {
        return 2 + chainA3(count);
    }

    private long chainA3(long count)
    {
        return 3 + chainA4(count);
    }

    private long chainA4(long count)
    {
        // last link will not be inlined
        return bigMethod(count, 4);
    }

    private long chainB1(long count)
    {
        return chainB2(count) - 1;
    }

    private long chainB2(long count)
    {
        return chainB3(count) - 2;
    }

    private long chainB3(long count)
    {
        return count - 3;
    }

    private long bigMethod(long count, int i)
    {
        long a, b, c, d, e, f, g;

        a = count;
        b = count;
        c = count;
        d = count;
        e = count;
        f = count;
        g = count;

        a += i;
        b += i;
        c += i;
        d += i;
        e += i;
        f += i;
        g += i;

        a += 1;
        b += 2;
        c += 3;
        d += 4;
        e += 5;
        f += 6;
        g += 7;

        a += i;
        b += i;
        c += i;
        d += i;
        e += i;
        f += i;
        g += i;

        a -= 7;
        b -= 6;
        c -= 5;
        d -= 4;
        e -= 3;
        f -= 2;
        g -= 1;

        a++;
        b++;
        c++;
        d++;
        e++;
        f++;
        g++;

        a /= 2;
        b /= 2;
        c /= 2;
        d /= 2;
        e /= 2;
        f /= 2;
        g /= 2;

        long result = a + b + c + d + e + f + g;

        return result;
    }
}
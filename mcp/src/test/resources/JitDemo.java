package demo;

/**
 * Small program that provokes six distinct HotSpot JIT behaviours so we can
 * see each one in JITWatch:
 *   1. a tiny method that gets INLINED
 *   2. a big method that is REFUSED for inlining ("hot method too big")
 *   3. an allocation that ESCAPE ANALYSIS removes entirely
 *   4. a MONOMORPHIC virtual call (one receiver type) vs a MEGAMORPHIC one (three types)
 *   5. an INTRINSIC (Math.min replaced by a CPU instruction)
 *   6. a DEOPTIMISATION: a branch the JIT assumed never happens, then does
 */
public class JitDemo
{
    // ---------- 4. shapes for the virtual-call test ----------
    interface Shape { double area(); }
    static final class Circle implements Shape { final double r; Circle(double r){this.r=r;} public double area(){ return Math.PI * r * r; } }
    static final class Square implements Shape { final double s; Square(double s){this.s=s;} public double area(){ return s * s; } }
    static final class Tri    implements Shape { final double b, h; Tri(double b,double h){this.b=b;this.h=h;} public double area(){ return 0.5 * b * h; } }

    // ---------- 3. object for the escape-analysis test ----------
    static final class Point { final int x, y; Point(int x, int y){ this.x = x; this.y = y; } }

    // 1. tiny: 4 bytes of bytecode, will be inlined everywhere it is called
    static long add(long a, int b) { return a + b; }

    // 2. big: ~350 bytes of bytecode, above FreqInlineSize (325) so C2 refuses to inline it
    static long big(long c, int i)
    {
        long a=c,b=c,d=c,e=c,f=c,g=c,h=c;
        a+=i; b+=i; d+=i; e+=i; f+=i; g+=i; h+=i;
        a+=1; b+=2; d+=3; e+=4; f+=5; g+=6; h+=7;
        a-=i; b-=i; d-=i; e-=i; f-=i; g-=i; h-=i;
        a*=3; b*=3; d*=3; e*=3; f*=3; g*=3; h*=3;
        a+=i; b+=i; d+=i; e+=i; f+=i; g+=i; h+=i;
        a-=7; b-=6; d-=5; e-=4; f-=3; g-=2; h-=1;
        a^=i; b^=i; d^=i; e^=i; f^=i; g^=i; h^=i;
        a+=b; d+=e; f+=g; a+=d; f+=h; a+=f;
        return a;
    }

    // 3. allocates a Point that never leaves the method -> C2 eliminates the allocation
    static int sumOfPoint(int i)
    {
        Point p = new Point(i, i + 1);
        return p.x + p.y;
    }

    // 4a. only ever called with Circle -> monomorphic, the call is inlined
    static double areaMono(Shape s) { return s.area(); }

    // 4b. called with three types -> megamorphic, cannot be inlined
    static double areaMega(Shape s) { return s.area(); }

    // 5. Math.min is an intrinsic: C2 emits a cmov instead of calling a method
    static int minLoop(int[] a)
    {
        int m = Integer.MAX_VALUE;
        for (int v : a) m = Math.min(m, v);
        return m;
    }

    // 6. for the first million calls the branch is never taken, so C2 compiles
    //    it as "never happens" (uncommon trap). Then we take it -> deoptimisation.
    static int branchy(int i, int threshold)
    {
        if (i > threshold) return -i;
        return i;
    }

    public static void main(String[] args)
    {
        final int N = 1_000_000;

        // 1 + 2
        long sum = 0;
        for (int i = 0; i < N; i++) { sum = add(sum, i); sum = big(sum, i); }
        System.out.println("add+big     : " + sum);

        // 3
        long ea = 0;
        for (int i = 0; i < N; i++) ea += sumOfPoint(i);
        System.out.println("escape      : " + ea);

        // 4
        Shape[] mono = { new Circle(1), new Circle(2), new Circle(3) };
        Shape[] mega = { new Circle(1), new Square(2), new Tri(3, 4) };
        double am = 0, ag = 0;
        for (int i = 0; i < N; i++) { am += areaMono(mono[i % 3]); ag += areaMega(mega[i % 3]); }
        System.out.println("mono/mega   : " + am + " " + ag);

        // 5
        int[] arr = new int[64];
        for (int i = 0; i < arr.length; i++) arr[i] = (i * 7919) % 1000;
        long mn = 0;
        for (int i = 0; i < N; i++) mn += minLoop(arr);
        System.out.println("intrinsic   : " + mn);

        // 6: warm up with threshold never exceeded, then exceed it
        long br = 0;
        for (int i = 0; i < N; i++) br += branchy(i, Integer.MAX_VALUE);
        for (int i = 0; i < 1000; i++) br += branchy(i, 10);
        System.out.println("deopt       : " + br);
    }
}

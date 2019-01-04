import static java.lang.System.out;

public final class Class {

    public static void main(String[] args) {
        int a = cztery();
        int c = piec();
        double d = liczbaDouble();
        boolean e = prawda();
        char f = znak();
        long g = liczbaLong();
        String hi = czesc();
        int j = dodaj(a, c);
        int k = podwojna(c);
        out.println(a + c);
        out.println(d);
        out.println(e);
        out.println(f);
        out.println(g);
        out.println(hi);
        out.println(j);
        out.println(k);
    }

    public static int cztery() {
        return 4;
    }

    public static boolean prawda() {
        return true;
    }

    public static double liczbaDouble() {
        return 5.2;
    }

    public static char znak() {
        return 'a';
    }

    public static long liczbaLong() {
        return 1000000000;
    }

    public static int piec() {
        return 5;
    }

    public static String czesc() {
        return "czesc";
    }

    public static int dodaj(int a, int b){
        return a+b;
    }

    public static int podwojna(int a){
        return 2*a;
    }
}
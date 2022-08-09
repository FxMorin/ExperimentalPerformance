package classAnalysis.testClasses;

public class BigBoi {

    // Skip these:
    public static long IGNORE_ME_LONG = 0L;
    public static boolean IGNORE_ME_BOOL = false;
    private static int IGNORE_ME_INT = 42;
    private static double IGNORE_ME_DOUBLE = 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679D;
    protected static float IGNORE_ME_FLOAT = 6.62607015F;
    static Boolean IGNORE_ME_BOOLEAN = Boolean.TRUE;

    // Controls
    private int con2 = 2;
    public int con3 = 3;
    protected int con4 = 4;
    int con5 = 5;

    // See these and use them to optimize:
    private byte num1 = 1;
    private short num2 = 2;
    private int num3 = 3;
    private long num4 = 4;
    private float num5 = 5;
    private double num6 = 6;
    private boolean bool1 = false;
    private char char1 = 'a';
    private String str1 = "hi";

    // See these but don't touch:
    public byte pnum1 = 1;
    public short pnum2 = 2;
    public int pnum3 = 3;
    public long pnum4 = 4;
    public float pnum5 = 5;
    public double pnum6 = 6;
    public boolean pbool1 = false;
    public char pchar1 = 'a';
    public String pstr1 = "hi";
}

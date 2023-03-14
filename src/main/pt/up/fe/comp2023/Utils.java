package pt.up.fe.comp2023;

public class Utils {
    public static boolean debug = false;
    public static int sizeOfDelimiter = 40;

    public static void printHeader(String s) {
        if (s.length() > sizeOfDelimiter-2) return;
        boolean oneMore = s.length() % 2 != 0;
        var n = (sizeOfDelimiter - s.length()-2) / 2;
        String header = "-".repeat(n) + " " + s + " " + "-".repeat(n) + (oneMore ? "-" : "");
        System.out.println("\n" + header);
    }

    public static void printFooter() {
        System.out.println("-".repeat(sizeOfDelimiter) + "\n");
    }
}

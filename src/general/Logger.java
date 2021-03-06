package general;

public class Logger {

    public static final int WARNING = 0;
    public static final int ERROR = -1;
    public static final int INFO = 1;

    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    private static final int loglevellevel = 2;

    /**
     * log default message // same as sout
     * @param message message to print
     */
    public void log(String message) {

        System.out.println(message);
    }

    /**
     * log a message with  a specific type
     * @param message the message to print
     * @param type    the type of message (ERROR,WARNING;INFO)
     */
    public static void log(String message, int type) {
        log(message, type, 1);
    }

    /**
     * log a message with  a specific type and log level
     * @param message message to print
     * @param type    the type of message (ERROR,WARNING;INFO)
     * @param level   log level 0 - 10
     */
    public static void log(String message, int type, int level) {
        if (level <= loglevellevel) {
            if (System.getProperty("os.name").contains("Windows")) //colored terminal in Windows not supported...
            {
                switch (type) {
                    case 1:
                        System.out.println("[INFO] " + message);
                        break;
                    case 0:
                        System.out.println("[WARNING] " + message);
                        break;
                    case -1:
                        System.out.println("[ERROR] " + message);
                        break;
                }
            } else {
                switch (type) {
                    case 1:
                        System.out.println(CYAN + "[INFO] " + message + RESET);
                        break;
                    case 0:
                        System.out.println(YELLOW + "[WARNING] " + message + RESET);
                        break;
                    case -1:
                        System.out.println(RED + "[ERROR] " + message + RESET);
                        break;
                }
            }
        }
    }
}

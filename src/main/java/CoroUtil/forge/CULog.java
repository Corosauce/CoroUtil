package CoroUtil.forge;

import CoroUtil.config.ConfigCoroAI;

public class CULog {

    /**
     * For seldom used but important things to print out in production
     *
     * @param string
     */
    public static void log(String string) {
        if (ConfigCoroAI.useLoggingLog) {
            System.out.println(string);
        }
    }

    /**
     * For logging warnings/errors
     *
     * @param string
     */
    public static void err(String string) {
        if (ConfigCoroAI.useLoggingError) {
            System.out.println(string);
        }
    }

    /**
     * For debugging things
     *
     * @param string
     */
    public static void dbg(String string) {
        if (ConfigCoroAI.useLoggingDebug) {
            System.out.println(string);
        }
    }

}

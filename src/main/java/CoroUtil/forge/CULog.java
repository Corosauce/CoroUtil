package CoroUtil.forge;

import CoroUtil.config.ConfigCoroUtil;

public class CULog {

    /**
     * For seldom used but important things to print out in production
     *
     * @param string
     */
    public static void log(String string) {
        if (ConfigCoroUtil.useLoggingLog) {
            System.out.println(string);
        }
    }

    /**
     * For logging warnings/errors
     *
     * @param string
     */
    public static void err(String string) {
        if (ConfigCoroUtil.useLoggingError) {
            System.out.println(string);
        }
    }

    /**
     * For debugging things
     *
     * @param string
     */
    public static void dbg(String string) {
        if (ConfigCoroUtil.useLoggingDebug) {
            System.out.println(string);
        }
    }

}

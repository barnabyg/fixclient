package com.blizzardtec.fixclient;

/**
 * Fix client main class.
 *
 */
public final class FixClientApp {

    /**
     * Utility class has private constructor.
     */
    private FixClientApp() {
        // private constructor
    }

    /**
     * Main app start.
     *
     * @param args
     *            command line args
     */
    public static void main(final String[] args) {

        final FixClient fixClient = new FixClient();

        fixClient.run();
    }
}

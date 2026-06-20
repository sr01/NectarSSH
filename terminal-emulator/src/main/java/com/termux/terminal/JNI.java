package com.termux.terminal;

/**
 * Stub for native methods. Not used in NectarSSH (we use SSH streams instead of local PTY).
 * Kept to avoid modifying other Termux source files that reference this class.
 */
final class JNI {

    public static int createSubprocess(String cmd, String cwd, String[] args, String[] envVars, int[] processId, int rows, int columns, int cellWidth, int cellHeight) {
        throw new UnsupportedOperationException("Local subprocess not supported - use SSH streams");
    }

    public static void setPtyWindowSize(int fd, int rows, int cols, int cellWidth, int cellHeight) {
        // No-op for SSH mode
    }

    public static int waitFor(int processId) {
        throw new UnsupportedOperationException("Local subprocess not supported - use SSH streams");
    }

    public static void close(int fileDescriptor) {
        // No-op for SSH mode
    }

}

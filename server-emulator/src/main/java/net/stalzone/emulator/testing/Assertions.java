package net.stalzone.emulator.testing;

import java.util.Objects;

/**
 * A minimal, dependency-free assertion library. Exists because this project's sandbox has no
 * Maven Central access (see server-emulator/README.md's "Why no Netty" section for the same
 * constraint applied elsewhere) so a real JUnit dependency can't be fetched -- same reasoning
 * that led to {@code json/JsonReader.java} being hand-written instead of using Gson/similar.
 * Just enough to write real, runnable regression tests for this project's own evidenced logic.
 */
public final class Assertions {

    private Assertions() {
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " -- expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertEquals(double expected, double actual, double epsilon, String message) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError(message + " -- expected <" + expected + "> but was <" + actual + "> (epsilon " + epsilon + ")");
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " -- expected true");
        }
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message + " -- expected false");
        }
    }

    public static void assertNull(Object value, String message) {
        if (value != null) {
            throw new AssertionError(message + " -- expected null but was <" + value + ">");
        }
    }

    public static void assertNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message + " -- expected non-null");
        }
    }

    public static void assertThrows(Class<? extends Throwable> expectedType, ThrowingRunnable body, String message) {
        try {
            body.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return;
            }
            throw new AssertionError(message + " -- expected " + expectedType.getSimpleName()
                    + " but caught " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
        throw new AssertionError(message + " -- expected " + expectedType.getSimpleName() + " but nothing was thrown");
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}

package net.stalzone.emulator.testing;

/** One runnable test body, throwing on failure (an {@link AssertionError} from {@link Assertions}, or any other exception). */
@FunctionalInterface
public interface TestCase {
    void run() throws Exception;
}

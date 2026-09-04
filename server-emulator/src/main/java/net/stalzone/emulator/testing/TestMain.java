package net.stalzone.emulator.testing;

/**
 * CLI entry point for this project's dependency-free test suite -- runs every test in
 * {@link AllTests#all()} and exits non-zero if any failed, so it's usable as a CI gate even
 * without a real build tool (no Maven/Gradle available in this project's original sandbox -- see
 * server-emulator/README.md's "Why no Netty" section).
 *
 * Usage:
 *   java -cp out net.stalzone.emulator.testing.TestMain
 */
public final class TestMain {

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        boolean allPassed = TestRunner.runAll(AllTests.all());
        System.exit(allPassed ? 0 : 1);
    }
}

package net.stalzone.emulator.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a flat list of named {@link TestCase}s, printing PASS/FAIL per test and a summary line,
 * same spirit as this project's *DumpTool classes -- a plain CLI, no framework magic, no
 * annotations/reflection scanning (this project has no dependency access to fetch JUnit -- see
 * {@link Assertions}'s doc). Each package's tests live in a {@code *Test.java} class exposing a
 * static {@code tests()} method returning a {@code List<TestRunner.NamedTest>}; {@link
 * net.stalzone.emulator.testing.AllTests} collects every package's list and runs them together.
 */
public final class TestRunner {

    private TestRunner() {
    }

    public static final class NamedTest {
        public final String name;
        public final TestCase test;

        public NamedTest(String name, TestCase test) {
            this.name = name;
            this.test = test;
        }
    }

    /** Convenience to build one {@link NamedTest} without importing the constructor everywhere. */
    public static NamedTest test(String name, TestCase test) {
        return new NamedTest(name, test);
    }

    /** Runs every test, printing results as it goes. Returns true iff every test passed. */
    public static boolean runAll(List<NamedTest> tests) {
        int passed = 0;
        List<String> failures = new ArrayList<>();
        for (NamedTest t : tests) {
            try {
                t.test.run();
                System.out.println("PASS  " + t.name);
                passed++;
            } catch (Throwable e) {
                System.out.println("FAIL  " + t.name + " -- " + e.getMessage());
                failures.add(t.name);
            }
        }
        System.out.println();
        System.out.println(passed + " passed, " + failures.size() + " failed, " + tests.size() + " total");
        if (!failures.isEmpty()) {
            System.out.println("Failed: " + failures);
        }
        return failures.isEmpty();
    }
}

package net.stalzone.emulator.tips;

import net.stalzone.emulator.testing.TestRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link AdviceList}, using fixtures modeled on the real files' confirmed
 * structure (part 51): CRLF-terminated, blank-line-separated paragraphs, no trailing newline.
 */
public final class AdviceListTest {

    private static final String FIXTURE_3_TIPS =
            "First tip here.\r\n\r\nSecond tip, two lines,\r\nstill one tip.\r\n\r\nThird and last tip.";

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("AdviceList.load splits on blank-line-separated paragraphs, CRLF-terminated, no trailing newline", () -> {
            AdviceList advices = loadFixture(FIXTURE_3_TIPS);
            assertEquals(3, advices.size(), "tip count");
            assertEquals("First tip here.", advices.tips().get(0), "tip 0");
            assertEquals("Third and last tip.", advices.tips().get(2), "tip 2 (last, no trailing newline)");
        }));

        out.add(test("AdviceList.load keeps a multi-line tip as one entry, CRLF normalized to LF", () -> {
            AdviceList advices = loadFixture(FIXTURE_3_TIPS);
            assertEquals("Second tip, two lines,\nstill one tip.", advices.tips().get(1), "multi-line tip 1");
        }));

        out.add(test("AdviceList.load also accepts bare-LF-separated fixtures (tolerant of a re-saved copy)", () -> {
            AdviceList advices = loadFixture("Tip A.\n\nTip B.");
            assertEquals(2, advices.size(), "tip count (LF fixture)");
            assertEquals("Tip A.", advices.tips().get(0), "tip 0 (LF fixture)");
        }));

        out.add(test("AdviceList.load rejects a file with no tip text", () -> {
            assertThrows(java.io.IOException.class, () -> loadFixture(""), "empty file should throw IOException");
        }));

        return out;
    }

    private static AdviceList loadFixture(String content) throws Exception {
        Path tmp = Files.createTempFile("advice-list-fixture", ".txt");
        tmp.toFile().deleteOnExit();
        Files.writeString(tmp, content, StandardCharsets.UTF_8);
        return AdviceList.load(tmp);
    }
}

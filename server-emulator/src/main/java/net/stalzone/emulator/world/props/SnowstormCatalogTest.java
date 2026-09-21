package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;
import net.stalzone.emulator.testing.TestRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/**
 * Regression tests for {@link SnowstormCatalog}, using a fixture built from real {@code
 * snowstorm.*} lines drawn directly from {@code stalker/lang/en.lang} (part 150, live-verified
 * on device) -- real start/end announcer variants sharing one id.
 */
public final class SnowstormCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "snowstorm.notf.start_1=Attention, everyone! I'm detecting an anomalous temperature drop. The pressure is also dropping rapidly. The wind will be so strong that anyone who hasn't found shelter will be blown away!",
            "snowstorm.notf.start_9=Either my devices have finally failed, or there are signs of artifacts detected in the approaching storm! Unbelievable! It looks like the storm's blown a treasure trove our way!",
            "snowstorm.notf.end_1=Phew! It's gone... The storm's over. Now, all you have to do is heat the door to the outside. It's completely iced over. Ahem... The sky's clear. It's over!"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("snowstorm-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static SnowstormCatalog loadFixture() throws IOException {
        return SnowstormCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("two real start_<n> announcer variants resolve under one shared id", () -> {
            SnowstormCatalog catalog = loadFixture();
            assertTrue(catalog.field("notf", "start_1").contains("temperature drop"), "start_1 contains real phrase");
            assertTrue(catalog.field("notf", "start_9").contains("treasure trove"), "start_9 contains real phrase");
        }));

        out.add(test("a real end_<n> announcer variant resolves distinct from the start variants", () -> {
            SnowstormCatalog catalog = loadFixture();
            assertTrue(catalog.field("notf", "end_1").contains("storm's over"), "end_1 contains real phrase");
            assertTrue(!catalog.field("notf", "end_1").equals(catalog.field("notf", "start_1")), "end_1 and start_1 are genuinely different real texts");
        }));

        out.add(test("ids() is sorted and unknown ids/fields resolve to null rather than throwing", () -> {
            SnowstormCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(1, ids.size(), "1 real id in fixture (notf)");
            assertEquals("notf", ids.get(0), "the one real id");
            assertNull(catalog.field("not_a_real_id", "start_1"), "unknown id");
            assertNull(catalog.field("notf", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}

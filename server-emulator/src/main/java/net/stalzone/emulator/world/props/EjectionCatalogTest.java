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
 * Regression tests for {@link EjectionCatalog}, using a fixture built from real {@code
 * ejection.*} lines drawn directly from {@code stalker/lang/en.lang} (part 141, live-verified on
 * device) -- real numbered {@code stage.notf} flavor-text variants across two of the five real
 * phase categories, the real winter/fallout announcer pairs, and the real shelter status toggle.
 */
public final class EjectionCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "ejection.stage.notf.storm_1=Attention! The eruption is starting. Hang in there, fellas!",
            "ejection.stage.notf.storm_2=Alright. Here we go! The eruption is starting. You snooze, you lose!",
            "ejection.stage.notf.preparation_1=Attention, everyone! An eruption is approaching.",
            "ejection.winter.notification_prepare.1=Attention! The ecologists are predicting a cold snap. Winter is closing in!",
            "ejection.winter.notification_start.1=Caliph's nose has frozen! Get out your heaters and incendiary rounds!",
            "ejection.fallout.notification_prepare=Attention! Stalkers, the wind is blowing from the Quarry again. You have a few minutes.",
            "ejection.fallout.notification_start=Radioactive fallout is here. I hope you managed to find cover.",
            "ejection.shelter.msg.enter=You are in cover",
            "ejection.shelter.msg.exit=You are not in cover"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("ejection-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static EjectionCatalog loadFixture() throws IOException {
        return EjectionCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("real numbered stage.notf flavor-text variants across two phase categories share one id", () -> {
            EjectionCatalog catalog = loadFixture();
            assertEquals("Attention! The eruption is starting. Hang in there, fellas!", catalog.field("stage.notf", "storm_1"), "stage.notf storm_1");
            assertEquals("Alright. Here we go! The eruption is starting. You snooze, you lose!", catalog.field("stage.notf", "storm_2"), "stage.notf storm_2");
            assertEquals("Attention, everyone! An eruption is approaching.", catalog.field("stage.notf", "preparation_1"), "stage.notf preparation_1");
        }));

        out.add(test("the real winter and fallout announcer pairs resolve as two genuinely separate real events", () -> {
            EjectionCatalog catalog = loadFixture();
            assertEquals("Attention! The ecologists are predicting a cold snap. Winter is closing in!",
                    catalog.field("winter.notification_prepare", "1"), "winter.notification_prepare.1");
            assertEquals("Caliph's nose has frozen! Get out your heaters and incendiary rounds!",
                    catalog.field("winter.notification_start", "1"), "winter.notification_start.1");
            assertEquals("Attention! Stalkers, the wind is blowing from the Quarry again. You have a few minutes.",
                    catalog.field("fallout", "notification_prepare"), "fallout notification_prepare");
            assertEquals("Radioactive fallout is here. I hope you managed to find cover.",
                    catalog.field("fallout", "notification_start"), "fallout notification_start");
        }));

        out.add(test("the real shelter status toggle resolves, ids() is sorted, and unknown ids/fields resolve to null rather than throwing", () -> {
            EjectionCatalog catalog = loadFixture();
            assertEquals("You are in cover", catalog.field("shelter.msg", "enter"), "shelter.msg enter");
            assertEquals("You are not in cover", catalog.field("shelter.msg", "exit"), "shelter.msg exit");
            List<String> ids = catalog.ids();
            assertEquals(5, ids.size(), "5 real ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.field("not_a_real_id", "1"), "unknown id");
            assertNull(catalog.field("stage.notf", "not_a_real_field"), "unknown field on a real id");
        }));

        return out;
    }
}

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
 * Regression tests for {@link FactionBaseWarningCatalog}, using a fixture built from real lines
 * drawn directly from {@code stalker/lang/en.lang} (part 124, live-verified on device) -- one real
 * line from each of the five real faction-warning prefixes, plus a deliberately-omitted faction id
 * to confirm the class tolerates a faction with no data instead of throwing.
 */
public final class FactionBaseWarningCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "merc.protected_areas.alert.notification.0=You're approaching the Mercenary base, stranger. \\nWe'll give you one chance: turn around, or we'll fill you full of bullets.",
            "merc.protected_areas.alert.notification.1=Attention! Security perimeter breached. \\nAll Mercenaries to their posts: we shoot to kill intruders.",
            "duty.protected_areas.alert.notification.0=Warning, intruder! \\nTurn back, or we'll open fire.",
            "freedom.protected_areas.alert.notification.0=Hey, you! You're not respecting Rise's boundaries. \\nMove it before you get a bullet in the head.",
            "covenant.protected_areas.alert.notification.0=Unbeliever! You are on Covenant land!\\nTurn aside, or feel the divine wrath!",
            "bandit.protected_areas.alert.notification.0=Oi, dumbass! You got the wrong base or something? \\nMove it, or we'll kill you!",
            "bandit.protected_areas.alert.notification.1=Whose base do you think this is, you moron?! \\nWe're gonna shoot!"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("faction-base-warning-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static FactionBaseWarningCatalog loadFixture() throws IOException {
        return FactionBaseWarningCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("merc's two real warning lines resolve in index order", () -> {
            FactionBaseWarningCatalog catalog = loadFixture();
            List<String> lines = catalog.notifications("merc");
            assertEquals(2, lines.size(), "merc has 2 real lines in fixture");
            assertTrue(lines.get(0).startsWith("You're approaching the Mercenary base"), "merc line 0");
            assertTrue(lines.get(1).startsWith("Attention! Security perimeter breached"), "merc line 1");
        }));

        out.add(test("each of the other four real factions resolves its own distinct voice", () -> {
            FactionBaseWarningCatalog catalog = loadFixture();
            assertTrue(catalog.notifications("duty").get(0).contains("Turn back, or we'll open fire"), "duty voice");
            assertTrue(catalog.notifications("freedom").get(0).contains("Rise's boundaries"), "freedom voice");
            assertTrue(catalog.notifications("covenant").get(0).contains("divine wrath"), "covenant voice");
            assertEquals(2, catalog.notifications("bandit").size(), "bandit has 2 real lines in fixture");
        }));

        out.add(test("factionIds() is sorted and reflects only factions with real data", () -> {
            FactionBaseWarningCatalog catalog = loadFixture();
            List<String> ids = catalog.factionIds();
            assertEquals(5, ids.size(), "5 real factions in fixture");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
        }));

        out.add(test("an unknown or data-less faction id resolves to an empty list rather than throwing", () -> {
            FactionBaseWarningCatalog catalog = loadFixture();
            assertEquals(0, catalog.notifications("not_a_real_faction").size(), "unknown faction id");
        }));

        return out;
    }
}

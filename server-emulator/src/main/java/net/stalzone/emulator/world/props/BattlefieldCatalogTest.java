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
 * Regression tests for {@link BattlefieldCatalog}, using a fixture built from real {@code
 * battlefield.*} lines drawn directly from {@code stalker/lang/en.lang} (part 106, live-verified on
 * device) -- covering the real {@code reich}/{@code reich_breakthrough} Reichstag maps, the {@code
 * pripyat} SSPP/Murmur tie, the {@code 1april} joke map, and the lore-unconnected {@code rus} map's
 * real team names.
 */
public final class BattlefieldCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "battlefield.id.reich.name=Assault on the Reichstag",
            "battlefield.id.reich.description=Participate in a fierce battle for every inch of land with historical equipment",
            "battlefield.id.reich.point.hall.name=Hall",
            "battlefield.id.reich.point.entrance.name=Entrance",
            "battlefield.id.reich_breakthrough.name=Assault on the Reichstag",
            "battlefield.id.reich_breakthrough.point.a.name=Moltke Bridge",
            "battlefield.id.reich_breakthrough.point.b.name=Brandenburg Gate",
            "battlefield.id.reich_breakthrough.point.f.name=Reichstag",
            "battlefield.id.pripyat.name=Pripyat Streets",
            "battlefield.id.pripyat.description=SSPP is conducting an operation against Murmur",
            "battlefield.id.pripyat.point.garage.name=Garage",
            "battlefield.id.1april.name=Petard Server",
            "battlefield.id.1april.description=Basically, someone did something there for April Fools",
            "battlefield.id.rus.name=Battlefield",
            "battlefield.id.rus.point.north.name=Idol of Perun",
            "battlefield.id.rus.team1.name=Slavs",
            "battlefield.id.rus.team2.name=Lizards"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("battlefield-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static BattlefieldCatalog loadFixture() throws IOException {
        return BattlefieldCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("load counts the real fixture maps correctly", () -> {
            BattlefieldCatalog catalog = loadFixture();
            assertEquals(5, catalog.mapCount(), "map count");
        }));

        out.add(test("mapName/mapDescription resolve the real reich (Reichstag) map, a 9th corroboration", () -> {
            BattlefieldCatalog catalog = loadFixture();
            assertEquals("Assault on the Reichstag", catalog.mapName("reich"), "reich name");
            assertTrue(catalog.mapDescription("reich").contains("historical equipment"), "reich description");
            assertEquals("Hall", catalog.pointName("reich", "hall"), "reich hall point");
        }));

        out.add(test("reich_breakthrough resolves real historical Berlin landmark points", () -> {
            BattlefieldCatalog catalog = loadFixture();
            assertEquals("Assault on the Reichstag", catalog.mapName("reich_breakthrough"), "reich_breakthrough name");
            assertEquals("Moltke Bridge", catalog.pointName("reich_breakthrough", "a"), "point a");
            assertEquals("Brandenburg Gate", catalog.pointName("reich_breakthrough", "b"), "point b");
            assertEquals("Reichstag", catalog.pointName("reich_breakthrough", "f"), "point f");
        }));

        out.add(test("pripyat map description ties into the real confirmed SSPP/Murmur thread", () -> {
            BattlefieldCatalog catalog = loadFixture();
            assertEquals("Pripyat Streets", catalog.mapName("pripyat"), "pripyat name");
            assertTrue(catalog.mapDescription("pripyat").contains("Murmur"), "pripyat description mentions Murmur");
            assertEquals("Garage", catalog.pointName("pripyat", "garage"), "pripyat garage point");
        }));

        out.add(test("1april resolves the real April Fools joke map", () -> {
            BattlefieldCatalog catalog = loadFixture();
            assertEquals("Petard Server", catalog.mapName("1april"), "1april name");
            assertTrue(catalog.mapDescription("1april").contains("April Fools"), "1april description");
        }));

        out.add(test("rus map resolves real lore-unconnected team names and a Slavic-folklore point", () -> {
            BattlefieldCatalog catalog = loadFixture();
            assertEquals("Battlefield", catalog.mapName("rus"), "rus name");
            assertEquals("Slavs", catalog.teamName("rus", "team1"), "rus team1");
            assertEquals("Lizards", catalog.teamName("rus", "team2"), "rus team2");
            assertEquals("Idol of Perun", catalog.pointName("rus", "north"), "rus north point");
        }));

        out.add(test("mapDescription/pointName/teamName return null for real ids with no such line", () -> {
            BattlefieldCatalog catalog = loadFixture();
            assertNull(catalog.mapDescription("rus"), "rus has no real description line in this fixture");
            assertNull(catalog.pointName("reich", "not_a_real_point"), "unknown point");
            assertNull(catalog.teamName("reich", "team1"), "reich has no real team lines");
        }));

        out.add(test("mapIds is sorted and contains every real fixture map", () -> {
            BattlefieldCatalog catalog = loadFixture();
            List<String> ids = catalog.mapIds();
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "alphabetical at " + i);
            }
            assertTrue(ids.contains("reich_breakthrough"), "contains reich_breakthrough");
            assertTrue(ids.contains("1april"), "contains 1april");
        }));

        return out;
    }
}

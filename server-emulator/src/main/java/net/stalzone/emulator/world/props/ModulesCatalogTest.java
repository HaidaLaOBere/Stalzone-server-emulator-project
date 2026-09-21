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
 * Regression tests for {@link ModulesCatalog}, using a fixture built from real {@code
 * modules.*} lines drawn directly from {@code stalker/lang/en.lang} (part 138, live-verified on
 * device) -- a real plain-description module, the real {@code _pre}/{@code _suf} affix pair, and
 * a real {@code .behaviour.description} on-hit module.
 */
public final class ModulesCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "modules.gyroscope.description=significantly reduces recoil buildup",
            "modules.recoil_pre.description=reduces recoil",
            "modules.recoil_suf.description=increases recoil",
            "modules.gas_bullets.behaviour.description=Every hit causes a gas cloud which deals damage over time to opponents"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("modules-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static ModulesCatalog loadFixture() throws IOException {
        return ModulesCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("a real plain-description module resolves", () -> {
            ModulesCatalog catalog = loadFixture();
            assertEquals("significantly reduces recoil buildup", catalog.description("gyroscope"), "gyroscope description");
        }));

        out.add(test("the real _pre/_suf affix pair resolves as opposite real modifier texts", () -> {
            ModulesCatalog catalog = loadFixture();
            assertEquals("reduces recoil", catalog.description("recoil_pre"), "recoil_pre description");
            assertEquals("increases recoil", catalog.description("recoil_suf"), "recoil_suf description");
        }));

        out.add(test("a real .behaviour.description on-hit module resolves as its own distinct id, and ids() is sorted with null-safe lookups", () -> {
            ModulesCatalog catalog = loadFixture();
            assertEquals("Every hit causes a gas cloud which deals damage over time to opponents", catalog.description("gas_bullets.behaviour"), "gas_bullets.behaviour description");
            List<String> ids = catalog.ids();
            assertEquals(4, ids.size(), "4 real module ids in fixture");
            assertTrue(ids.get(0).compareTo(ids.get(ids.size() - 1)) < 0, "sorted ascending");
            assertNull(catalog.description("not_a_real_module"), "unknown module id");
        }));

        return out;
    }
}

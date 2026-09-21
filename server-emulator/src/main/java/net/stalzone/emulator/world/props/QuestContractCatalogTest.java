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
 * Regression tests for {@link QuestContractCatalog}, using a fixture built from real {@code
 * quest_contract.*} lines drawn directly from {@code stalker/lang/en.lang} (part 117, live-verified
 * on device) -- covering the real name/title pair, an id with only one of the two fields, and the
 * real flat contract-giver-label exceptions.
 */
public final class QuestContractCatalogTest {

    private static final String FIXTURE_CONTENT = String.join("\n",
            "quest_contract.wnorth_sniper.name=Murmur Assassination",
            "quest_contract.wnorth_sniper.title=Murmur Neutralization",
            "quest_contract.quest_unical_items_sever_7.name=Request for rare items",
            "quest_contract.quest_unical_items_sever_7.title=Relay Station Module",
            "quest_contract.quest_weekly_sever_3.name=General assignment",
            "quest_contract.quest_weekly_sever_3.title=Partner's Contract",
            "quest_contract.sever_new_contract1=\"Take And Forget\" Tasks",
            "quest_contract.sever_new_contract2=Partner's Tasks",
            "quest_contract.sever_new_contract3=Seva Ford Tasks",
            "quest_contract.sever_new_contract4=Pu'er Tasks",
            "quest_contract.sever_new_contract5=Jaeger Tasks",
            "quest_contract.sever_new_contract6=Curie Tasks"
    ) + "\n";

    private static Path writeFixture() throws IOException {
        Path file = Files.createTempFile("quest-contract-catalog-fixture", ".lang");
        file.toFile().deleteOnExit();
        Files.writeString(file, FIXTURE_CONTENT);
        return file;
    }

    private static QuestContractCatalog loadFixture() throws IOException {
        return QuestContractCatalog.load(LangCatalog.load(writeFixture()));
    }

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("name/title resolve together for a real dual-field quest id", () -> {
            QuestContractCatalog catalog = loadFixture();
            assertEquals("Murmur Assassination", catalog.name("wnorth_sniper"), "wnorth_sniper name");
            assertEquals("Murmur Neutralization", catalog.title("wnorth_sniper"), "wnorth_sniper title");
        }));

        out.add(test("field resolves generically for a real long compound id", () -> {
            QuestContractCatalog catalog = loadFixture();
            assertEquals("Request for rare items", catalog.name("quest_unical_items_sever_7"), "sever_7 name");
            assertEquals("Relay Station Module", catalog.title("quest_unical_items_sever_7"), "sever_7 title");
        }));

        out.add(test("contractGiverLabel resolves the real flat named-NPC exceptions", () -> {
            QuestContractCatalog catalog = loadFixture();
            assertEquals("\"Take And Forget\" Tasks", catalog.contractGiverLabel("sever_new_contract1"), "contract1");
            assertEquals("Seva Ford Tasks", catalog.contractGiverLabel("sever_new_contract3"), "contract3 real named NPC");
            assertEquals("Pu'er Tasks", catalog.contractGiverLabel("sever_new_contract4"), "contract4 real named NPC");
            assertEquals("Jaeger Tasks", catalog.contractGiverLabel("sever_new_contract5"), "contract5 real named NPC");
            assertEquals("Curie Tasks", catalog.contractGiverLabel("sever_new_contract6"), "contract6 real named NPC");
        }));

        out.add(test("ids/fieldNames/contractGiverIds are sorted and reflect the real fixture data", () -> {
            QuestContractCatalog catalog = loadFixture();
            List<String> ids = catalog.ids();
            assertEquals(3, ids.size(), "3 real quest ids in fixture");
            for (int i = 1; i < ids.size(); i++) {
                assertTrue(ids.get(i - 1).compareTo(ids.get(i)) < 0, "sorted ascending");
            }
            assertEquals(2, catalog.fieldNames("wnorth_sniper").size(), "wnorth_sniper has name+title");
            assertEquals(6, catalog.contractGiverIds().size(), "6 real contract-giver ids");
        }));

        out.add(test("unknown ids/fields resolve to null/empty rather than throwing", () -> {
            QuestContractCatalog catalog = loadFixture();
            assertNull(catalog.name("not_a_real_quest"), "unknown quest name");
            assertNull(catalog.field("wnorth_sniper", "not_a_real_field"), "unknown field");
            assertNull(catalog.contractGiverLabel("not_a_real_contract"), "unknown contract giver");
            assertEquals(0, catalog.fieldNames("not_a_real_quest").size(), "unknown quest field names");
        }));

        return out;
    }
}

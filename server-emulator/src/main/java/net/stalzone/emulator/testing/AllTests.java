package net.stalzone.emulator.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects every package's {@code *Test.tests()} list into one flat suite. Adding a new
 * {@code *Test.java} class means adding one line here -- no reflection/classpath scanning (no
 * dependency access for a real framework that would do this automatically, see
 * {@link Assertions}'s doc), same explicit-registration style already used elsewhere in this
 * project (e.g. {@code items/ItemGroups.java}'s explicit category list).
 */
public final class AllTests {

    private AllTests() {
    }

    public static List<TestRunner.NamedTest> all() {
        List<TestRunner.NamedTest> all = new ArrayList<>();
        all.addAll(net.stalzone.emulator.items.WeaponRegistryTest.tests());
        all.addAll(net.stalzone.emulator.dialogue.NpcIdGroupingTest.tests());
        all.addAll(net.stalzone.emulator.mobs.MobInstanceTest.tests());
        all.addAll(net.stalzone.emulator.session.save.PlayerSaveFileTest.tests());
        all.addAll(net.stalzone.emulator.clientdata.QuestProgressTest.tests());
        all.addAll(net.stalzone.emulator.clientdata.WaypointListTest.tests());
        all.addAll(net.stalzone.emulator.clientdata.KeyBindingsTest.tests());
        all.addAll(net.stalzone.emulator.clientdata.ClientOptionsTest.tests());
        all.addAll(net.stalzone.emulator.clientdata.ClientPrefsTest.tests());
        all.addAll(net.stalzone.emulator.loot.LootTableTest.tests());
        all.addAll(net.stalzone.emulator.loot.LootTablesTest.tests());
        all.addAll(net.stalzone.emulator.loot.LootRollerTest.tests());
        all.addAll(net.stalzone.emulator.mobs.MobHealthTableTest.tests());
        all.addAll(net.stalzone.emulator.survival.MinFoodConfigTest.tests());
        all.addAll(net.stalzone.emulator.items.ItemTiersTest.tests());
        all.addAll(net.stalzone.emulator.tips.AdviceListTest.tests());
        all.addAll(net.stalzone.emulator.economy.TraderItemGroupsTest.tests());
        all.addAll(net.stalzone.emulator.economy.BarterItemCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ArmorModelCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ArtefactModelCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.BackpackModelCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ItemModelCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.CurrencyCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerGuideCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.KoreaJurisdictionCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.EffectsParticleCatalogTest.tests());
        return all;
    }
}

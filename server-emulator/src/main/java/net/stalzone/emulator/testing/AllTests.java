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
        all.addAll(net.stalzone.emulator.world.props.EffectsAssetCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.GloomycoreCatalogTest.tests());
        all.addAll(net.stalzone.emulator.platform.SteamDlcIdsTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PdaMapCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.HighpolyCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.WeaponAssetCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalcraftCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.CustomNpcsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerMobsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.CustomItemsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.AnomalyCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerClansCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerShopCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.GameObjectsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.VegetationCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.RagdollCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.AuctionCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.BundleCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.MiscAssetsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.LocalizedAssetsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.emission.EmissionCycleTest.tests());
        all.addAll(net.stalzone.emulator.session.WalletTest.tests());
        all.addAll(net.stalzone.emulator.progression.PlayerClassTest.tests());
        all.addAll(net.stalzone.emulator.progression.PrestigeAbilityTest.tests());
        all.addAll(net.stalzone.emulator.session.PlayerStateTest.tests());
        all.addAll(net.stalzone.emulator.config.ServerConfigTest.tests());
        all.addAll(net.stalzone.emulator.progression.QuestStatusTest.tests());
        all.addAll(net.stalzone.emulator.session.QuestLogTest.tests());
        all.addAll(net.stalzone.emulator.session.ConsoleDriverTest.tests());
        all.addAll(net.stalzone.emulator.session.PlayerStateSaveTest.tests());
        all.addAll(net.stalzone.emulator.mobs.SpawnSchedulerTest.tests());
        all.addAll(net.stalzone.emulator.modding.ModEventBusTest.tests());
        all.addAll(net.stalzone.emulator.modding.ModLoaderTest.tests());
        all.addAll(net.stalzone.emulator.localization.LangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerPlayerCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerSoundsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerMiscCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerStreamCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ModelMiscCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TextureMiscCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TextureLoadscreenCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TextureParticlesCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TextureArmorCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TextureItemsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TextureGuiCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerSoundMiscCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerCloseEnvironmentCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerSoundEventsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.LocationNameRegistryTest.tests());
        all.addAll(net.stalzone.emulator.world.SettlementRegistryTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PatchAssetsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.CraftRecipeCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.BattlefieldCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.HideoutCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.CnpsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ClanWarCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.CurrencyDisplayCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.LoadscreenCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.WorldEventCatalogTest.tests());
        all.addAll(net.stalzone.emulator.tips.DailyExcuseCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.OperationsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.HomeOutpostCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ShopCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ChallengesCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.QuestContractCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.GeneralCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.StalkerLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.GoLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ItemLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.GuiLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.FactionBaseWarningCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.SettlementPerkCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.MapLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.CheckpointsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.HandbookCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.FrontlineCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.WorldModifierCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PhonographCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TileCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.BpSeasonCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.RaventCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PartyMissionsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PrestigeLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.DmzLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.DeliveryCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.OrdersCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ModulesCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ItemOriginsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.SeasonPassCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.EjectionCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.SavepointsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PremiumCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ArcadeCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.SeasonsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.EncagedCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.HintCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.GameObjectsLangCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.IncidentCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.SnowstormCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.IndustriesCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ChallengeCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.ArenaCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.InsideCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.BlizzardCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.OutpostModeCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.NewYearEventCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.SeasonCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PersonalStorageCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.AccountBadgeCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.EconomyRolloutCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.WorldFlavorCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PlayerMessagingCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.OddsAndEndsCatalogTest.tests());
        all.addAll(net.stalzone.emulator.items.ItemLoreCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.Spring26ChallengeCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TutorialGuideCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.FrontlineBriefingCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.OpsLeagueCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.TriumphCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.OperationModeCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.PdaWaypointEventCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.AnnouncerCatalogTest.tests());
        all.addAll(net.stalzone.emulator.world.props.RegionLabelCatalogTest.tests());
        return all;
    }
}

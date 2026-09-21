package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalogs the real ~15 remaining single-line "prefixes" from part 123's follow-up census (part
 * 164, closing out that census in full) -- each of these 14 real top-level {@code en.lang}
 * prefixes contributes exactly ONE real line, too small individually to be worth a class of its
 * own, so this class exposes each as one named accessor over a direct key lookup rather than a
 * prefix-scanned table.
 *
 * <p><b>A real "signal" marker-name family</b> (6 lines): {@code signals.exit.name}="Exit", {@code
 * signal_chrono.name}="Chronorift", {@code signal_dungeon.name}="Dungeon", {@code
 * signal_wnorth.name}="ES-30", {@code signal_sequence.name}="Tangled", {@code
 * signal_sequence_beacon_key.name}="A", {@code signal_sequence_dungeon_key.name}="B" -- real PDA
 * signal-hunt waypoint/marker labels. {@code signal_chrono}="Chronorift" independently ties to
 * part 162's {@link WorldFlavorCatalog} real {@code anomaly.notf.anomaly_chrono.info}="A
 * chronodevice has been set up in this location" -- the same real "chrono" theme confirmed from
 * two separate real namespaces, disclosed as a link only, not merged.
 *
 * <p><b>A real, coherent 3-line questline snippet</b> naming two real, previously-uncatalogued
 * NPCs, "Burn" and "Art": {@code polygon7_hint.description}="First, Burn must leave the Mutation
 * Department.", {@code toxic_dogout_hint1.description}="The door is locked. Perhaps a key can be
 * found among Art's belongings.", {@code subtitles.quest.dungeon_toxic_dogout.text}="We need to
 * get out of here. Maybe it's worth searching for Art's Hideout." The real {@code
 * dungeon_toxic_dogout} id independently confirms part 26's {@link
 * net.stalzone.emulator.world.MilestoneMap} already-cached, already-visited real zone of the same
 * name -- these three real lines are quest/hint/subtitle text for that exact confirmed zone.
 *
 * <p><b>Real standalone status-effect/mode-gate odds and ends</b> (3 lines): {@code
 * player_effect.hunger.title}="Hunger" (a real, previously-uncatalogued survival status effect);
 * {@code effect.category.inside.phantoms}="Phantoms" (a real status-effect category name sharing
 * the "phantom" theme with part 154's {@link InsideCatalog} {@code phantom_cure_buffs} ability,
 * disclosed as a thematic link only); {@code
 * stalkerclans.battlefields.no_access.message}="The mode becomes available at Bar reputation
 * level 3" (a real mode-unlock gate message for the clan-war PvP mode whose ASSETS part 72's
 * {@link StalkerClansCatalog} already covers -- this is the first real {@code en.lang} TEXT for
 * that mode, a genuinely separate real namespace from that asset-folder class, not a duplicate).
 *
 * <p><b>A real orphaned-key oddity, disclosed exactly as found, not corrected.</b> {@code
 * filler.wild_mining_5.additional_stats_tip} sits at a bare top-level {@code filler.*} key with
 * NO {@code item.} prefix, immediately adjacent in the source file to part 121's {@link
 * ItemLangCatalog}-covered real {@code item.filler.wild_mining_5.name}="Broken Phase Battery" --
 * the SAME real item id, but this one field landed under a different, un-prefixed real top-level
 * namespace instead of alongside the rest of that item's fields under {@code item.filler.*}. This
 * project has no direct evidence of client intent here (a real data-entry slip vs. deliberate
 * namespace split); disclosed as an observed real oddity per standing rule 1, not asserted either
 * way.
 */
public final class OddsAndEndsCatalog {

    private final String signalsExit;
    private final String signalChrono;
    private final String signalDungeon;
    private final String signalWnorth;
    private final String signalSequence;
    private final String signalSequenceBeaconKey;
    private final String signalSequenceDungeonKey;
    private final String polygon7Hint;
    private final String toxicDogoutHint1;
    private final String dungeonToxicDogoutSubtitle;
    private final String playerEffectHungerTitle;
    private final String effectCategoryInsidePhantoms;
    private final String stalkerclansNoAccessMessage;
    private final String orphanedFillerWildMining5Tip;

    private OddsAndEndsCatalog(Map<String, String> raw) {
        this.signalsExit = raw.get("signals.exit.name");
        this.signalChrono = raw.get("signal_chrono.name");
        this.signalDungeon = raw.get("signal_dungeon.name");
        this.signalWnorth = raw.get("signal_wnorth.name");
        this.signalSequence = raw.get("signal_sequence.name");
        this.signalSequenceBeaconKey = raw.get("signal_sequence_beacon_key.name");
        this.signalSequenceDungeonKey = raw.get("signal_sequence_dungeon_key.name");
        this.polygon7Hint = raw.get("polygon7_hint.description");
        this.toxicDogoutHint1 = raw.get("toxic_dogout_hint1.description");
        this.dungeonToxicDogoutSubtitle = raw.get("subtitles.quest.dungeon_toxic_dogout.text");
        this.playerEffectHungerTitle = raw.get("player_effect.hunger.title");
        this.effectCategoryInsidePhantoms = raw.get("effect.category.inside.phantoms");
        this.stalkerclansNoAccessMessage = raw.get("stalkerclans.battlefields.no_access.message");
        this.orphanedFillerWildMining5Tip = raw.get("filler.wild_mining_5.additional_stats_tip");
    }

    private static final String[] KEYS = {
        "signals.exit.name",
        "signal_chrono.name",
        "signal_dungeon.name",
        "signal_wnorth.name",
        "signal_sequence.name",
        "signal_sequence_beacon_key.name",
        "signal_sequence_dungeon_key.name",
        "polygon7_hint.description",
        "toxic_dogout_hint1.description",
        "subtitles.quest.dungeon_toxic_dogout.text",
        "player_effect.hunger.title",
        "effect.category.inside.phantoms",
        "stalkerclans.battlefields.no_access.message",
        "filler.wild_mining_5.additional_stats_tip",
    };

    public static OddsAndEndsCatalog load(LangCatalog catalog) {
        Map<String, String> raw = new LinkedHashMap<>();
        for (String key : KEYS) {
            String value = catalog.get(key);
            if (value != null) {
                raw.put(key, value);
            }
        }
        return new OddsAndEndsCatalog(raw);
    }

    public String signalsExit() {
        return signalsExit;
    }

    public String signalChrono() {
        return signalChrono;
    }

    public String signalDungeon() {
        return signalDungeon;
    }

    public String signalWnorth() {
        return signalWnorth;
    }

    public String signalSequence() {
        return signalSequence;
    }

    public String signalSequenceBeaconKey() {
        return signalSequenceBeaconKey;
    }

    public String signalSequenceDungeonKey() {
        return signalSequenceDungeonKey;
    }

    public String polygon7Hint() {
        return polygon7Hint;
    }

    public String toxicDogoutHint1() {
        return toxicDogoutHint1;
    }

    public String dungeonToxicDogoutSubtitle() {
        return dungeonToxicDogoutSubtitle;
    }

    public String playerEffectHungerTitle() {
        return playerEffectHungerTitle;
    }

    public String effectCategoryInsidePhantoms() {
        return effectCategoryInsidePhantoms;
    }

    public String stalkerclansNoAccessMessage() {
        return stalkerclansNoAccessMessage;
    }

    public String orphanedFillerWildMining5Tip() {
        return orphanedFillerWildMining5Tip;
    }
}

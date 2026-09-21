package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Catalogs the real 202-line {@code settlement.id.*} namespace in {@code stalker/lang/en.lang}
 * (part 125, the second concrete namespace built from part 123's follow-up census). This is a
 * COMPLETELY DIFFERENT real namespace from {@link
 * net.stalzone.emulator.world.SettlementRegistry} (part 28) despite the shared English word
 * "settlement" -- that class models a bare 15-name client-log roster with no further detail; this
 * one is a real per-settlement REPUTATION-LEVEL progression system: flavor text for each
 * reputation rank plus the concrete gameplay perks (trader stock, storage, repair/research/
 * artifact-sale discounts) each rank unlocks. Confirmed distinct by direct content comparison, not
 * assumed from the shared name.
 *
 * <p>Real shape, confirmed by direct count to have no leftover lines: {@code settlement.id.
 * general.perk.<perkId>} (47 lines) is a flat, settlement-agnostic lookup of perk DESCRIPTION TEXT
 * BY VALUE (e.g. {@code research_cost_25}="25% discount on research", {@code art_price_50}="+50%
 * to the cost of selling artifacts and mutant parts") -- this is a generic value catalog, not tied
 * to any one settlement's actual unlock levels. The remaining 155 lines are organized per real
 * settlement/base id ({@code merc}, {@code freedom}, {@code duty}, {@code covenant}, {@code
 * sever}, {@code rostok}, {@code lodochnaya}, {@code wnorth_bar}, {@code garage}, {@code foton2},
 * {@code csky}, {@code battlefields}, {@code north_factions_block}, {@code bar_sever}, {@code
 * bar_save_residential_hangar}, {@code td}, {@code agro} -- the same 15 real ids {@link
 * net.stalzone.emulator.world.SettlementRegistry#ALL_KNOWN_SETTLEMENTS} already names, confirming
 * that crosswalk once more from an independent angle, plus the {@code sever} unprefixed alias for
 * {@code bar_sever} that class's own part-105 doc had already flagged): {@code .title} (16 lines),
 * {@code .default_desc} (14, the level-1 flavor text), {@code .level.<n>.desc} (62, level-2-and-up
 * flavor text), {@code .level.<n>.perk.<perkId>} (60, the specific perk(s) that settlement grants
 * at that level, with its OWN concrete value string -- not just a reference into {@code
 * general.perk.*}, since e.g. {@code csky.level.3.perk.art_price}="+10%..." while {@code
 * lodochnaya.level.3.perk.art_price}="+20%...", genuinely different values per settlement), and 1
 * real one-off ({@code battlefields.unavailableLine}="Not enough reputation", a locked-content UI
 * message).
 *
 * <p>{@code sever}'s own level ladder runs all the way to level 7 (deeper than the 5-level ladders
 * everywhere else), and includes {@code rostok.level.7.perk.sever}="Access to the Drunken Geiger" --
 * a real cross-settlement unlock: reaching level 7 reputation at Rostok grants entry to the Bar's
 * "Drunken Geiger" area. This directly extends {@link net.stalzone.emulator.world.SettlementRegistry}'s
 * own part-107 update, which found (via a separate namespace, {@link CnpsCatalog}'s {@code
 * cnps.faction.settlement_<id>.relation.<level>}) that {@code rostok} and {@code wnorth} express
 * reputation through quoted dialogue rather than a formal rank noun -- this class supplies the
 * matching real flavor-text and perk-unlock side of that same progression, from a third
 * independent {@code en.lang} source.
 */
public final class SettlementPerkCatalog {

    private final Map<String, String> generalPerkDescriptionById;
    private final Map<String, String> titleBySettlementId;
    private final Map<String, String> defaultDescBySettlementId;
    private final Map<String, Map<Integer, String>> levelDescBySettlementId;
    private final Map<String, Map<Integer, Map<String, String>>> levelPerkBySettlementId;
    private final Map<String, Map<String, String>> extraFieldsBySettlementId;

    private SettlementPerkCatalog(
            Map<String, String> generalPerkDescriptionById,
            Map<String, String> titleBySettlementId,
            Map<String, String> defaultDescBySettlementId,
            Map<String, Map<Integer, String>> levelDescBySettlementId,
            Map<String, Map<Integer, Map<String, String>>> levelPerkBySettlementId,
            Map<String, Map<String, String>> extraFieldsBySettlementId) {
        this.generalPerkDescriptionById = generalPerkDescriptionById;
        this.titleBySettlementId = titleBySettlementId;
        this.defaultDescBySettlementId = defaultDescBySettlementId;
        this.levelDescBySettlementId = levelDescBySettlementId;
        this.levelPerkBySettlementId = levelPerkBySettlementId;
        this.extraFieldsBySettlementId = extraFieldsBySettlementId;
    }

    private static final String PREFIX = "settlement.id.";
    private static final Pattern LEVEL_DESC = Pattern.compile("^level\\.(\\d+)\\.desc$");
    private static final Pattern LEVEL_PERK = Pattern.compile("^level\\.(\\d+)\\.perk\\.(.+)$");

    public static SettlementPerkCatalog load(LangCatalog catalog) {
        Map<String, String> generalPerkDescriptionById = new LinkedHashMap<>();
        Map<String, String> titleBySettlementId = new LinkedHashMap<>();
        Map<String, String> defaultDescBySettlementId = new LinkedHashMap<>();
        Map<String, Map<Integer, String>> levelDescBySettlementId = new LinkedHashMap<>();
        Map<String, Map<Integer, Map<String, String>>> levelPerkBySettlementId = new LinkedHashMap<>();
        Map<String, Map<String, String>> extraFieldsBySettlementId = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int firstDot = rest.indexOf('.');
            if (firstDot < 0) {
                continue;
            }
            String id = rest.substring(0, firstDot);
            String tail = rest.substring(firstDot + 1);
            String value = catalog.get(key);

            if (id.equals("general") && tail.startsWith("perk.")) {
                generalPerkDescriptionById.put(tail.substring("perk.".length()), value);
                continue;
            }

            if (tail.equals("title")) {
                titleBySettlementId.put(id, value);
                continue;
            }
            if (tail.equals("default_desc")) {
                defaultDescBySettlementId.put(id, value);
                continue;
            }

            Matcher levelDescMatch = LEVEL_DESC.matcher(tail);
            if (levelDescMatch.matches()) {
                int level = Integer.parseInt(levelDescMatch.group(1));
                levelDescBySettlementId.computeIfAbsent(id, k -> new TreeMap<>()).put(level, value);
                continue;
            }

            Matcher levelPerkMatch = LEVEL_PERK.matcher(tail);
            if (levelPerkMatch.matches()) {
                int level = Integer.parseInt(levelPerkMatch.group(1));
                String perkId = levelPerkMatch.group(2);
                levelPerkBySettlementId.computeIfAbsent(id, k -> new TreeMap<>())
                        .computeIfAbsent(level, k -> new LinkedHashMap<>())
                        .put(perkId, value);
                continue;
            }

            extraFieldsBySettlementId.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(tail, value);
        }

        return new SettlementPerkCatalog(generalPerkDescriptionById, titleBySettlementId,
                defaultDescBySettlementId, levelDescBySettlementId, levelPerkBySettlementId,
                extraFieldsBySettlementId);
    }

    /** The generic, settlement-agnostic perk description for this perk value id (from {@code general.perk.*}). */
    public String generalPerkDescription(String perkId) { return generalPerkDescriptionById.get(perkId); }

    public String title(String settlementId) { return titleBySettlementId.get(settlementId); }
    public String defaultDesc(String settlementId) { return defaultDescBySettlementId.get(settlementId); }

    /** The real flavor text for reaching this reputation level at this settlement (level 2+), or null. */
    public String levelDesc(String settlementId, int level) {
        Map<Integer, String> byLevel = levelDescBySettlementId.get(settlementId);
        return byLevel == null ? null : byLevel.get(level);
    }

    /** Real reputation levels (2+) with disclosed flavor text for this settlement, ascending. */
    public List<Integer> levelsWithDesc(String settlementId) {
        Map<Integer, String> byLevel = levelDescBySettlementId.get(settlementId);
        return byLevel == null ? List.of() : new ArrayList<>(byLevel.keySet());
    }

    /** This settlement's own concrete perk-value text unlocked at this level, for this perk id, or null. */
    public String levelPerk(String settlementId, int level, String perkId) {
        Map<Integer, Map<String, String>> byLevel = levelPerkBySettlementId.get(settlementId);
        if (byLevel == null) {
            return null;
        }
        Map<String, String> byPerk = byLevel.get(level);
        return byPerk == null ? null : byPerk.get(perkId);
    }

    /** Real perk ids this settlement unlocks at this level, sorted. */
    public List<String> levelPerkIds(String settlementId, int level) {
        Map<Integer, Map<String, String>> byLevel = levelPerkBySettlementId.get(settlementId);
        if (byLevel == null) {
            return List.of();
        }
        Map<String, String> byPerk = byLevel.get(level);
        if (byPerk == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>(byPerk.keySet());
        Collections.sort(out);
        return out;
    }

    /** A one-off real field not covered by title/default_desc/level.*  (e.g. {@code unavailableLine}), or null. */
    public String extraField(String settlementId, String field) {
        Map<String, String> extras = extraFieldsBySettlementId.get(settlementId);
        return extras == null ? null : extras.get(field);
    }

    /** Real settlement ids with at least a title, sorted. */
    public List<String> settlementIds() {
        List<String> out = new ArrayList<>(titleBySettlementId.keySet());
        Collections.sort(out);
        return out;
    }
}

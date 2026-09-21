package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 2,020-line {@code stalker.*} namespace in {@code stalker/lang/en.lang} (part
 * 119) -- named identically to the game's own {@code stalker/} asset folder this project's many
 * other {@code Stalker*Catalog} classes model (file/folder-based), but this is a wholly separate
 * real thing: the {@code stalker.} PREFIX inside the localization file itself. Confirmed by direct
 * count that the namespace splits into exactly 14 real top-level groups with no leftover lines:
 * {@code quickcommands} (914), {@code navigation} (331), {@code ui_guide} (264), {@code hud} (233),
 * {@code guide} (116), {@code gui} (48), {@code clans} (24), {@code alliance} (21), {@code
 * protected_areas} (18), {@code battlefield} (17), {@code dmz2026} (14), {@code conditional_guide}
 * (14), {@code artefact_properties} (4), {@code player_params} (2); sum = 2,020.
 *
 * <p>Two groups share a short id vocabulary with already-catalogued standalone namespaces but are
 * confirmed real, distinct content, not duplicates: {@code stalker.clans.tournament.*} (event-TYPE
 * descriptions and reward-mail templates for {@code brawl}/{@code 1v1}/{@code base_capture}) versus
 * the standalone {@code clans.tournament.*} (part 108's {@link ClanWarCatalog}, real per-MAP/mode
 * display names using the same three short ids as sub-ids of a bigger id); and {@code
 * stalker.battlefield.team_name.*} (real in-match SQUAD names) versus the standalone {@code
 * battlefield.*} (part 106's {@link BattlefieldCatalog}, real MAP names).
 *
 * <p>{@code stalker.battlefield.team_name.redarmy}="Red Army" and {@code .vermacht}="Wehrmacht",
 * plus {@code stalker.hud.breakthrough.team.attackers}="RED ARMY" / {@code .defenders}="WEHRMACHT"
 * and a real {@code stalker.gui.arcades.results.breakthrough.*}/{@code
 * stalker.gui.seasonpass.event.reichstag.industry.*} reward-UI block (the latter reconfirming part
 * 109's 4-class Reichstag XP economy -- infantry/scout/machinegunner/engineer -- by name), are all
 * disclosed per the part-109 standing note as further uncounted entries in the long Reichstag
 * corroboration chain, no new ordinal asserted.
 *
 * <p>{@code quickcommands.*} (914 of 2,020 lines) is a real per-character-voice-pack "quick chat"
 * phrase-bank system: a voice-pack id ({@code stalkers}/{@code bandits}/{@code duty}/{@code
 * freedom}/{@code merc}/{@code covenant}/{@code terentii} at 100 lines each; {@code wehrmacht}/
 * {@code rkka} at 50 each, a real WWII-army quick-chat pack tied to the Reichstag mode; {@code
 * lizard}/{@code rusi}/{@code hunter} with irregular smaller counts) each carrying up to 10 real
 * phrase CATEGORIES ({@code hello}/{@code approv}/{@code disapprov}/{@code thanks}/{@code sorry}/
 * {@code attack}/{@code cover}/{@code med}/{@code ammo}/{@code surrender}), each an ordered list of
 * numbered phrase variants. 5 real flat exceptions (no voice-pack/category/index structure at all)
 * are exposed separately: {@code happynewyear} and three {@code approv/mark/<type>} ping labels
 * (id/item/world_event/game_object -- note the literal {@code /} characters embedded in these ids,
 * not path separators).
 *
 * <p>{@code navigation.transition.<from>-<to>.name} (331 lines) is a real previously-uncatalogued
 * inter-location fast-travel/route connector graph, naming named waypoints between zone-code pairs
 * (e.g. {@code agroprom-dungeon_mozglyak}="Weakling's Den").
 *
 * <p>{@code alliance.*} (21 lines) is real faction-allegiance UI text distinct from part 107's
 * {@link CnpsCatalog} (which carries short faction names and reputation ranks, not this content):
 * {@code gui.description.short.<factionId>}/{@code .full.<factionId>} (12 lines, 6 factions) give
 * real short taglines and real quoted full lore blurbs attributed to named faction leaders (e.g.
 * duty's full description is attributed to "Davydov, Frontier Commander"; bandits' to "Saint,
 * leader of the bandits"; stalkers' to "Ataman, leader of the stalkers" -- corroborating `csky`'s
 * "Ataman's HQ" settlement title from part 105 with a named leader). A flat {@code quest_join}
 * (1 line, faction-agnostic) plus real per-faction {@code <factionId>.quest_join}/{@code
 * .quest_leave} pairs (8 lines, for {@code duty}/{@code freedom}/{@code merc}/{@code covenant}
 * only -- {@code stalkers}/{@code bandits} have no join/leave quest text, consistent with them
 * being baseline rather than joinable factions elsewhere in this project) round out the group.
 *
 * <p>{@code ui_guide.*} (264), {@code guide.*} (116, covering both a {@code default.*} and a
 * {@code conditional.*} sub-group), {@code gui.*} (48), {@code clans.*} (24), and {@code
 * conditional_guide.*} (14, a real namespace DISTINCT from {@code guide.conditional.*} despite the
 * similar name -- disclosed as two separate real things, not merged) are each modeled with the
 * same generic last-dot-split {@code (id, field)} table this project has used since part 111,
 * since each has its own real internal nesting depth that a single fixed schema can't capture
 * cleanly.
 *
 * <p>{@code protected_areas.*} (18 lines) is a real faction-base trespass-warning system: a
 * flat 12-entry {@code owner} map naming which faction/settlement controls each of 12 real
 * protected areas, plus real ordered guard dialogue lists for an overlay prompt (1 line) and
 * escalating spoken warnings (5 lines, using the real in-universe slur "scofflaw" for trespassers).
 *
 * <p>{@code dmz2026.stat.<id>.name} (14 lines), {@code artefact_properties.factor.<id>}/{@code
 * .desc} (4 lines, a real flat label+desc pair distinct from the generic-table groups since the
 * label itself carries no field suffix), and {@code player_params.effect.category.<id>} (2 lines)
 * round out the namespace as small real flat label groups.
 */
public final class StalkerLangCatalog {

    private final Map<String, Map<String, List<String>>> quickCommandLinesByGroup;
    private final Map<String, String> quickCommandFlatById;

    private final Map<String, String> transitionNameById;

    private final Map<String, Map<String, String>> uiGuideFieldsById;

    private final Map<String, String> narrativeTooltipTextById;
    private final Map<String, String> breakthroughTeamNameById;

    private final Map<String, Map<String, String>> guideFieldsById;

    private final Map<String, Map<String, String>> guiFieldsById;

    private final Map<String, Map<String, String>> clansFieldsById;

    private final Map<String, Map<String, String>> allianceDescriptionById;
    private final Map<String, Map<String, String>> allianceQuestLabelById;
    private final String allianceGenericQuestJoinLabel;

    private final List<String> protectedAreaAlertOverlay;
    private final List<String> protectedAreaAlertNotification;
    private final Map<String, String> protectedAreaOwnerById;

    private final Map<String, String> battlefieldTeamNameById;

    private final Map<String, String> dmz2026StatNameById;

    private final Map<String, Map<String, String>> conditionalGuideFieldsById;

    private final Map<String, String> artefactPropertyLabelById;
    private final Map<String, String> artefactPropertyDescById;

    private final Map<String, String> playerParamsEffectCategoryLabelById;

    private StalkerLangCatalog(
            Map<String, Map<String, List<String>>> quickCommandLinesByGroup,
            Map<String, String> quickCommandFlatById,
            Map<String, String> transitionNameById,
            Map<String, Map<String, String>> uiGuideFieldsById,
            Map<String, String> narrativeTooltipTextById,
            Map<String, String> breakthroughTeamNameById,
            Map<String, Map<String, String>> guideFieldsById,
            Map<String, Map<String, String>> guiFieldsById,
            Map<String, Map<String, String>> clansFieldsById,
            Map<String, Map<String, String>> allianceDescriptionById,
            Map<String, Map<String, String>> allianceQuestLabelById,
            String allianceGenericQuestJoinLabel,
            List<String> protectedAreaAlertOverlay,
            List<String> protectedAreaAlertNotification,
            Map<String, String> protectedAreaOwnerById,
            Map<String, String> battlefieldTeamNameById,
            Map<String, String> dmz2026StatNameById,
            Map<String, Map<String, String>> conditionalGuideFieldsById,
            Map<String, String> artefactPropertyLabelById,
            Map<String, String> artefactPropertyDescById,
            Map<String, String> playerParamsEffectCategoryLabelById) {
        this.quickCommandLinesByGroup = quickCommandLinesByGroup;
        this.quickCommandFlatById = quickCommandFlatById;
        this.transitionNameById = transitionNameById;
        this.uiGuideFieldsById = uiGuideFieldsById;
        this.narrativeTooltipTextById = narrativeTooltipTextById;
        this.breakthroughTeamNameById = breakthroughTeamNameById;
        this.guideFieldsById = guideFieldsById;
        this.guiFieldsById = guiFieldsById;
        this.clansFieldsById = clansFieldsById;
        this.allianceDescriptionById = allianceDescriptionById;
        this.allianceQuestLabelById = allianceQuestLabelById;
        this.allianceGenericQuestJoinLabel = allianceGenericQuestJoinLabel;
        this.protectedAreaAlertOverlay = protectedAreaAlertOverlay;
        this.protectedAreaAlertNotification = protectedAreaAlertNotification;
        this.protectedAreaOwnerById = protectedAreaOwnerById;
        this.battlefieldTeamNameById = battlefieldTeamNameById;
        this.dmz2026StatNameById = dmz2026StatNameById;
        this.conditionalGuideFieldsById = conditionalGuideFieldsById;
        this.artefactPropertyLabelById = artefactPropertyLabelById;
        this.artefactPropertyDescById = artefactPropertyDescById;
        this.playerParamsEffectCategoryLabelById = playerParamsEffectCategoryLabelById;
    }

    private static final String PREFIX = "stalker.";

    public static StalkerLangCatalog load(LangCatalog catalog) {
        Map<String, Map<String, List<String>>> quickCommandLinesByGroup = new LinkedHashMap<>();
        Map<String, String> quickCommandFlatById = new LinkedHashMap<>();
        Map<String, String> transitionNameById = new LinkedHashMap<>();
        Map<String, Map<String, String>> uiGuideFieldsById = new LinkedHashMap<>();
        Map<String, String> narrativeTooltipTextById = new LinkedHashMap<>();
        Map<String, String> breakthroughTeamNameById = new LinkedHashMap<>();
        Map<String, Map<String, String>> guideFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> guiFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> clansFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> allianceDescriptionById = new LinkedHashMap<>();
        Map<String, Map<String, String>> allianceQuestLabelById = new LinkedHashMap<>();
        String[] allianceGenericQuestJoinLabelHolder = new String[1];
        Map<Integer, String> protectedAreaAlertOverlayTmp = new LinkedHashMap<>();
        Map<Integer, String> protectedAreaAlertNotificationTmp = new LinkedHashMap<>();
        Map<String, String> protectedAreaOwnerById = new LinkedHashMap<>();
        Map<String, String> battlefieldTeamNameById = new LinkedHashMap<>();
        Map<String, String> dmz2026StatNameById = new LinkedHashMap<>();
        Map<String, Map<String, String>> conditionalGuideFieldsById = new LinkedHashMap<>();
        Map<String, String> artefactPropertyLabelById = new LinkedHashMap<>();
        Map<String, String> artefactPropertyDescById = new LinkedHashMap<>();
        Map<String, String> playerParamsEffectCategoryLabelById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            String value = catalog.get(key);
            int firstDot = rest.indexOf('.');
            if (firstDot < 0) {
                continue;
            }
            String group = rest.substring(0, firstDot);
            String tail = rest.substring(firstDot + 1);

            switch (group) {
                case "quickcommands": {
                    String[] parts = tail.split("\\.");
                    if (parts.length == 3 && parts[2].chars().allMatch(Character::isDigit)) {
                        quickCommandLinesByGroup
                                .computeIfAbsent(parts[0], k -> new LinkedHashMap<>())
                                .computeIfAbsent(parts[1], k -> new ArrayList<>())
                                .add(value);
                    } else {
                        quickCommandFlatById.put(tail, value);
                    }
                    break;
                }
                case "navigation": {
                    if (tail.startsWith("transition.") && tail.endsWith(".name")) {
                        String id = tail.substring("transition.".length(), tail.length() - ".name".length());
                        transitionNameById.put(id, value);
                    }
                    break;
                }
                case "ui_guide": {
                    putLastDotSplit(uiGuideFieldsById, tail, value);
                    break;
                }
                case "hud": {
                    if (tail.startsWith("narrative_tooltip.")) {
                        narrativeTooltipTextById.put(tail.substring("narrative_tooltip.".length()), value);
                    } else if (tail.startsWith("breakthrough.team.")) {
                        breakthroughTeamNameById.put(tail.substring("breakthrough.team.".length()), value);
                    }
                    break;
                }
                case "guide": {
                    putLastDotSplit(guideFieldsById, tail, value);
                    break;
                }
                case "gui": {
                    putLastDotSplit(guiFieldsById, tail, value);
                    break;
                }
                case "clans": {
                    putLastDotSplit(clansFieldsById, tail, value);
                    break;
                }
                case "alliance": {
                    if (tail.startsWith("gui.description.")) {
                        String remainder = tail.substring("gui.description.".length());
                        int dot = remainder.indexOf('.');
                        if (dot > 0) {
                            String variant = remainder.substring(0, dot);
                            String factionId = remainder.substring(dot + 1);
                            allianceDescriptionById.computeIfAbsent(factionId, k -> new LinkedHashMap<>())
                                    .put(variant, value);
                        }
                    } else if (tail.equals("quest_join")) {
                        allianceGenericQuestJoinLabelHolder[0] = value;
                    } else {
                        int dot = tail.indexOf('.');
                        if (dot > 0) {
                            String factionId = tail.substring(0, dot);
                            String field = tail.substring(dot + 1);
                            allianceQuestLabelById.computeIfAbsent(factionId, k -> new LinkedHashMap<>())
                                    .put(field, value);
                        }
                    }
                    break;
                }
                case "protected_areas": {
                    if (tail.startsWith("alert.overlay.")) {
                        int idx = Integer.parseInt(tail.substring("alert.overlay.".length()));
                        protectedAreaAlertOverlayTmp.put(idx, value);
                    } else if (tail.startsWith("alert.notification.")) {
                        int idx = Integer.parseInt(tail.substring("alert.notification.".length()));
                        protectedAreaAlertNotificationTmp.put(idx, value);
                    } else if (tail.endsWith(".owner")) {
                        String areaId = tail.substring(0, tail.length() - ".owner".length());
                        protectedAreaOwnerById.put(areaId, value);
                    }
                    break;
                }
                case "battlefield": {
                    if (tail.startsWith("team_name.")) {
                        battlefieldTeamNameById.put(tail.substring("team_name.".length()), value);
                    }
                    break;
                }
                case "dmz2026": {
                    if (tail.startsWith("stat.") && tail.endsWith(".name")) {
                        String id = tail.substring("stat.".length(), tail.length() - ".name".length());
                        dmz2026StatNameById.put(id, value);
                    }
                    break;
                }
                case "conditional_guide": {
                    putLastDotSplit(conditionalGuideFieldsById, tail, value);
                    break;
                }
                case "artefact_properties": {
                    if (tail.startsWith("factor.")) {
                        String remainder = tail.substring("factor.".length());
                        if (remainder.endsWith(".desc")) {
                            artefactPropertyDescById.put(
                                    remainder.substring(0, remainder.length() - ".desc".length()), value);
                        } else {
                            artefactPropertyLabelById.put(remainder, value);
                        }
                    }
                    break;
                }
                case "player_params": {
                    if (tail.startsWith("effect.category.")) {
                        playerParamsEffectCategoryLabelById.put(
                                tail.substring("effect.category.".length()), value);
                    }
                    break;
                }
                default:
                    break;
            }
        }

        List<String> overlay = new ArrayList<>();
        protectedAreaAlertOverlayTmp.keySet().stream().sorted()
                .forEach(i -> overlay.add(protectedAreaAlertOverlayTmp.get(i)));
        List<String> notification = new ArrayList<>();
        protectedAreaAlertNotificationTmp.keySet().stream().sorted()
                .forEach(i -> notification.add(protectedAreaAlertNotificationTmp.get(i)));

        return new StalkerLangCatalog(
                quickCommandLinesByGroup, quickCommandFlatById, transitionNameById, uiGuideFieldsById,
                narrativeTooltipTextById, breakthroughTeamNameById, guideFieldsById, guiFieldsById,
                clansFieldsById, allianceDescriptionById, allianceQuestLabelById,
                allianceGenericQuestJoinLabelHolder[0], overlay, notification, protectedAreaOwnerById,
                battlefieldTeamNameById, dmz2026StatNameById, conditionalGuideFieldsById,
                artefactPropertyLabelById, artefactPropertyDescById, playerParamsEffectCategoryLabelById);
    }

    private static void putLastDotSplit(Map<String, Map<String, String>> target, String tail, String value) {
        int lastDot = tail.lastIndexOf('.');
        if (lastDot < 0) {
            return;
        }
        String id = tail.substring(0, lastDot);
        String field = tail.substring(lastDot + 1);
        target.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, value);
    }

    private static String genericField(Map<String, Map<String, String>> table, String id, String field) {
        Map<String, String> fields = table.get(id);
        return fields == null ? null : fields.get(field);
    }

    private static List<String> sortedKeys(Map<String, ?> map) {
        List<String> out = new ArrayList<>(map.keySet());
        Collections.sort(out);
        return out;
    }

    // --- quickcommands ---

    public String quickCommandLine(String voicePackId, String category, int index) {
        Map<String, List<String>> byCategory = quickCommandLinesByGroup.get(voicePackId);
        if (byCategory == null) {
            return null;
        }
        List<String> lines = byCategory.get(category);
        if (lines == null || index < 0 || index >= lines.size()) {
            return null;
        }
        return lines.get(index);
    }

    public int quickCommandLineCount(String voicePackId, String category) {
        Map<String, List<String>> byCategory = quickCommandLinesByGroup.get(voicePackId);
        if (byCategory == null) {
            return 0;
        }
        List<String> lines = byCategory.get(category);
        return lines == null ? 0 : lines.size();
    }

    public List<String> quickCommandCategories(String voicePackId) {
        Map<String, List<String>> byCategory = quickCommandLinesByGroup.get(voicePackId);
        return byCategory == null ? List.of() : sortedKeys(byCategory);
    }

    public List<String> quickCommandVoicePackIds() {
        return sortedKeys(quickCommandLinesByGroup);
    }

    /** Real flat exceptions ({@code happynewyear}, {@code approv/mark/<type>}), keyed as-is. */
    public String quickCommandFlat(String id) {
        return quickCommandFlatById.get(id);
    }

    // --- navigation ---

    public String transitionName(String fromZoneId, String toZoneId) {
        return transitionNameById.get(fromZoneId + "-" + toZoneId);
    }

    public String transitionNameById(String compoundId) {
        return transitionNameById.get(compoundId);
    }

    public List<String> transitionIds() {
        return sortedKeys(transitionNameById);
    }

    // --- ui_guide / guide / gui / clans / conditional_guide (generic) ---

    public String uiGuideField(String id, String field) { return genericField(uiGuideFieldsById, id, field); }
    public List<String> uiGuideIds() { return sortedKeys(uiGuideFieldsById); }

    public String guideField(String id, String field) { return genericField(guideFieldsById, id, field); }
    public List<String> guideIds() { return sortedKeys(guideFieldsById); }

    public String guiField(String id, String field) { return genericField(guiFieldsById, id, field); }
    public List<String> guiIds() { return sortedKeys(guiFieldsById); }

    public String clansField(String id, String field) { return genericField(clansFieldsById, id, field); }
    public List<String> clansIds() { return sortedKeys(clansFieldsById); }

    public String conditionalGuideField(String id, String field) {
        return genericField(conditionalGuideFieldsById, id, field);
    }
    public List<String> conditionalGuideIds() { return sortedKeys(conditionalGuideFieldsById); }

    // --- hud ---

    public String narrativeTooltipText(String posterId) { return narrativeTooltipTextById.get(posterId); }
    public List<String> narrativeTooltipIds() { return sortedKeys(narrativeTooltipTextById); }

    public String breakthroughTeamName(String side) { return breakthroughTeamNameById.get(side); }

    // --- alliance ---

    public String allianceDescription(String factionId, String variant) {
        Map<String, String> fields = allianceDescriptionById.get(factionId);
        return fields == null ? null : fields.get(variant);
    }

    public String allianceQuestLabel(String factionId, String event) {
        Map<String, String> fields = allianceQuestLabelById.get(factionId);
        return fields == null ? null : fields.get(event);
    }

    public String allianceGenericQuestJoinLabel() { return allianceGenericQuestJoinLabel; }

    public List<String> allianceJoinableFactionIds() { return sortedKeys(allianceQuestLabelById); }

    // --- protected_areas ---

    public List<String> protectedAreaAlertOverlay() { return protectedAreaAlertOverlay; }
    public List<String> protectedAreaAlertNotification() { return protectedAreaAlertNotification; }
    public String protectedAreaOwner(String areaId) { return protectedAreaOwnerById.get(areaId); }
    public List<String> protectedAreaIds() { return sortedKeys(protectedAreaOwnerById); }

    // --- battlefield ---

    public String battlefieldTeamName(String id) { return battlefieldTeamNameById.get(id); }
    public List<String> battlefieldTeamIds() { return sortedKeys(battlefieldTeamNameById); }

    // --- dmz2026 ---

    public String dmz2026StatName(String id) { return dmz2026StatNameById.get(id); }
    public List<String> dmz2026StatIds() { return sortedKeys(dmz2026StatNameById); }

    // --- artefact_properties ---

    public String artefactPropertyLabel(String id) { return artefactPropertyLabelById.get(id); }
    public String artefactPropertyDesc(String id) { return artefactPropertyDescById.get(id); }
    public List<String> artefactPropertyIds() { return sortedKeys(artefactPropertyLabelById); }

    // --- player_params ---

    public String playerParamsEffectCategoryLabel(String id) {
        return playerParamsEffectCategoryLabelById.get(id);
    }
    public List<String> playerParamsEffectCategoryIds() {
        return sortedKeys(playerParamsEffectCategoryLabelById);
    }
}

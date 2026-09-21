package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 191-line {@code gui.operation.how_to_play.*} namespace in {@code
 * stalker/lang/en.lang} (part 113) -- the in-client "how to play" tutorial/lore tree for a real PvE
 * game mode, "Operations" ({@code mode.mode_operations.name=Operations}: "A PvE game mode designed
 * for 3 players, where they complete a chosen linear dungeon with unique mechanics and bosses").
 * This directly strengthens one previously-disclosed lead and directly resolves another:
 * {@code breach.absorber.name=Photon Generator Power Supply} is a literal real confirmation that
 * the id {@code absorber} does mean "Photon Generator" (its {@code breach.absorber.desc}, "collect
 * anomalous particles and compress them into containers... extract the batteries, and place them in
 * generators", matches {@code operation.hud.absorber.header.desc}'s "Extract batteries from
 * attractors to power the generator" from part 104's {@link StalkerSoundEventsCatalog} find) -- this
 * closes the "not literal-word confirmed" gap that find left open for {@code absorber}/"Photon
 * Generator" itself, though it does not by itself confirm that find's separate, still-plausible-only
 * {@code foton2/} sound-folder-name coincidence. Separately, this namespace's {@code category}
 * crosswalk is a further, independent (third) real corroboration of the 3 real PvE Operation names
 * first confirmed at part 94's {@link StalkerStreamCatalog} and already independently corroborated
 * once at part 97's {@link TextureLoadscreenCatalog} via its {@code operation_big_cleanup}/{@code
 * operation_sea_alienation}/{@code operation_shock_therapy} texture folders: {@code
 * category.breach}="The Big Clean Up Operation" (=id {@code operation_big_cleanup}), {@code
 * category.focus}="Focus Operation" (=id {@code operation_shock_therapy}), {@code
 * category.sea_alienation}="Sea of Exclusion Operation" (=id {@code operation_sea_alienation}) --
 * all three names match exactly across all three independent real sources.
 *
 * <p><b>UPDATED part 115:</b> {@link ShopCatalog}'s {@code shop.categories.mk.title=SEA OF
 * EXCLUSION} is a fourth independent real corroboration of this same Operation name, tying the
 * in-game shop's own "mk" category code directly to this Operation. That same class's {@code
 * shop.categories.mt.title=ASSAULT ON THE REICHSTAG} separately ties the shop's "mt" category code
 * to the confirmed Reichstag battlefield -- disclosed there per this project's part-109 standing
 * note as simply another entry in that long corroboration chain, with no new ordinal asserted.
 *
 * <p>The namespace splits into seven real sub-groupings, each with a different real shape -- this
 * class deliberately does not force one schema onto all of them:
 *
 * <ul>
 * <li>{@code category.<id>} (7 lines) is a flat {@code id -> label} crosswalk with no dotted field
 * suffix at all -- real UI tab labels: {@code operation}="Operations Mode", {@code
 * enemies}="Enemies", {@code rewards}="Unique Rewards: Modules", {@code modifiers}="Difficulty
 * Modifiers", plus the three Operation-name entries above.</li>
 * <li>{@code mode.<id>.name}/{@code .desc} (32 lines, 16 ids) is a regular {@code (id, field)} table
 * describing the mode's own mechanics: {@code mode_access}="Clearance" (star/completion-time
 * difficulty progression), {@code mode_modifiers}="Difficulty Modifier" (weekly-rotating, unlocked at
 * Clearance 3/6/9), {@code mode_classes}="Classes" (three classes tied to armor: Assault
 * Trooper/Support/Operative), {@code mode_templates}="Gear Templates", {@code
 * mode_antichamber}="Lobby" (a safe zone with a "LIFT" mechanic to start a run), {@code
 * mode_restart}="Respawn Mechanism and 'Restart Stage' Button" (injector-based respawns -- Support
 * gets 3 injectors, Assault/Operative get 1), {@code mode_boss}="Bosses", {@code
 * mode_awards}="Reward for completion" (choose 1 of 3, weekly reset Wednesdays 5:00 UTC+3), {@code
 * mode_weekly_award}="Weekly Reward" (1/3/5 successful runs -&gt; 1/2/3 offers), {@code
 * mode_restricted_items}="Blocked Items", and a real "enemy ability timer" UI sub-family ({@code
 * mode_cast}="Enemy Ability Timer", {@code mode_breaking_cast}="Interrupting Enemy Abilities",
 * {@code mode_unbreakable_cast}="Uninterruptible Enemy Abilities", {@code
 * mode_important_cast}="Key enemy abilities" -- yellow-rhombus-marked, lock-icon-marked).</li>
 * <li>{@code enemies.<id>.*} (64 lines) is a real Murmur-faction combat roster, modeled generically
 * via {@link #enemyField} since -- like {@code we.*} in part 111 -- not every id carries the same
 * fields. Thirteen named fighters confirmed with real lore describing them as neurally/genetically
 * "xi-psyonic"-augmented zombie-soldiers: {@code assault}="Murmur Zombie", {@code front}="Armed
 * Murmur Zombie", {@code step}="Murmur Step", {@code suppressor}="Murmur Suppressor", {@code
 * gazr}="Murmur Gaze", {@code scream}="Murmur Growl", {@code Flash}="Murmur Flash", {@code
 * Glas}="Murmur Voice", {@code noise}="Murmur Noise", {@code mast}="Murmur Mastodon", {@code
 * electra_sniper}="Murmur Sight", {@code blinker}="Murmur Shadow", {@code
 * puppeteer}="Murmur Puppeteer". A real structural irregularity, honestly disclosed rather than
 * force-fit: every one of these fighters' individual ability title/text pairs (e.g. {@code
 * battle_cry.title}="Battle Cry", {@code drones.title}="Swarm Activation", {@code
 * sniper_shot.title}="Sniper Shot") is filed under one literal pseudo-id, {@code enemies.skills.*}
 * (38 of the 64 lines), rather than nested under the specific fighter id it narratively belongs to
 * -- so {@link #enemyField} naturally resolves these under the id {@code "skills"} rather than under
 * e.g. {@code "gazr"}.</li>
 * <li>{@code rewards.<id>.desc} (5 lines, only a {@code desc} field observed) describes a real weapon
 * "Module" crafting system: a Module = Add-on (positive) + Deviation (negative) + Concept
 * (positive/special) property, crafted from a "Module Core" at a "Noomachine" (found in the Bar and
 * at the Northern Factions' bases), optionally spending "Core Inhibitor" resources; a dismantled
 * Module can be bought back via a "Restore" window using a "Quasi-Destructor" tool, but only within
 * one day or it disappears forever.</li>
 * <li>{@code breach.<id>.*} / {@code focus.<id>.*} / {@code sea_alienation.<id>.*} (20 / 24 / 39
 * lines) each cover one real Operation's specific mechanics and named encounters, modeled generically
 * per operation via {@link #breachField}/{@link #focusField}/{@link #seaAlienationField}:
 * <ul>
 * <li>{@code breach} ("The Big Clean Up Operation"): {@code capture}="Capture of Communication
 * Hubs", {@code absorber}="Photon Generator Power Supply" (the part-104 lead, now resolved), and a
 * named elite-fighter boss, {@code ratter}="Ratcatcher", whose skills ARE nested under its own id
 * with a literal {@code .skills.} segment ({@code ratter.skills.whispers.title}="Murmur Mantras",
 * {@code ratter.skills.influence.title}="Murmur Influence", plus {@code gas_wall}/{@code
 * gas_floor}/{@code friends}/{@code self_heal}/{@code wrong} skills -- {@code
 * gas_wall.text=Pulp eaten} reads as leftover placeholder/debug text rather than finished copy,
 * disclosed as-is).</li>
 * <li>{@code focus} ("Focus Operation"): {@code escort}="Anomaly Escort" (a "Discharge" anomaly
 * escorted to charging nodes), {@code laser}="Laser Incision" (a "Mass laser accelerator" amplified
 * via the "Murmur Sight" ability), and a named unique boss, {@code electra}="Storm", with nine
 * nested skills under {@code electra.skills.*} (Armor/Battery/Leap/Beam/Wave/"Dance
 * Floor"/Shot/Lash/Stun).</li>
 * <li>{@code sea_alienation} ("Sea of Exclusion Operation"): real shipyard-siege mechanics --
 * {@code evade}="Manual Inspection" (a "Pennant team"/terminal switch-sequence puzzle), {@code
 * pressure_management}="Pressure Control" (airfield refueling), {@code match_glyphs}="Control
 * Nodes" (a hydraulic-crane terminal puzzle), {@code battleships}="Battleship" (remote-controlled
 * Yak-15/Mig-9 aircraft strikes), {@code ship_siege}="Assault on the Cruiser", {@code
 * cm51c}="Naval Artillery", {@code epmc}="Gauss EMCP-71", a named unique boss {@code
 * technician}="Tech" (20 lines incl. 7 nested {@code technician.skills.*} abilities), and a
 * name-only real boss lead, {@code quest_boss}="Captain Rozhin" (no {@code desc} line at all). The
 * same real irregularity found in {@code enemies} recurs here in miniature: two ship-hazard skills
 * ({@code ship_artillery.title}="Naval Salvo", {@code epmc71.title}="Prototype Shot") are filed
 * under the literal pseudo-id {@code sea_alienation.skills.*} rather than nested under {@code
 * ship_siege}/{@code epmc}, so {@link #seaAlienationField} resolves them under id {@code
 * "skills"}.</li>
 * </ul>
 * The {@code breach}/{@code focus} pattern (skills nested under the boss id via a literal {@code
 * .skills.} segment) and the {@code enemies}/{@code sea_alienation} pattern (skills filed under a
 * bare top-level {@code skills} id with no such segment) are two genuinely different real shapes
 * for the same underlying idea (a boss/enemy's individual abilities) -- disclosed as an
 * inconsistency in the source data, not merged or corrected.</li>
 * </ul>
 */
public final class OperationsCatalog {

    private final Map<String, String> categoryLabelById;
    private final Map<String, Map<String, String>> modeFieldsById;
    private final Map<String, Map<String, String>> enemyFieldsById;
    private final Map<String, Map<String, String>> rewardFieldsById;
    private final Map<String, Map<String, String>> breachFieldsById;
    private final Map<String, Map<String, String>> focusFieldsById;
    private final Map<String, Map<String, String>> seaAlienationFieldsById;

    private OperationsCatalog(Map<String, String> categoryLabelById,
                               Map<String, Map<String, String>> modeFieldsById,
                               Map<String, Map<String, String>> enemyFieldsById,
                               Map<String, Map<String, String>> rewardFieldsById,
                               Map<String, Map<String, String>> breachFieldsById,
                               Map<String, Map<String, String>> focusFieldsById,
                               Map<String, Map<String, String>> seaAlienationFieldsById) {
        this.categoryLabelById = categoryLabelById;
        this.modeFieldsById = modeFieldsById;
        this.enemyFieldsById = enemyFieldsById;
        this.rewardFieldsById = rewardFieldsById;
        this.breachFieldsById = breachFieldsById;
        this.focusFieldsById = focusFieldsById;
        this.seaAlienationFieldsById = seaAlienationFieldsById;
    }

    private static final String PREFIX = "gui.operation.how_to_play.";

    public static OperationsCatalog load(LangCatalog catalog) {
        Map<String, String> categoryLabelById = new LinkedHashMap<>();
        Map<String, Map<String, String>> modeFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> enemyFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> rewardFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> breachFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> focusFieldsById = new LinkedHashMap<>();
        Map<String, Map<String, String>> seaAlienationFieldsById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(PREFIX)) {
            String rest = key.substring(PREFIX.length());
            int groupDot = rest.indexOf('.');
            if (groupDot < 0) {
                continue;
            }
            String group = rest.substring(0, groupDot);
            String afterGroup = rest.substring(groupDot + 1);

            if (group.equals("category")) {
                // Flat id -> label crosswalk; the leaf value is the label itself, no field suffix.
                categoryLabelById.put(afterGroup, catalog.get(key));
                continue;
            }

            int idDot = afterGroup.indexOf('.');
            if (idDot < 0) {
                continue;
            }
            String id = afterGroup.substring(0, idDot);
            String field = afterGroup.substring(idDot + 1);

            Map<String, Map<String, String>> target;
            switch (group) {
                case "mode": target = modeFieldsById; break;
                case "enemies": target = enemyFieldsById; break;
                case "rewards": target = rewardFieldsById; break;
                case "breach": target = breachFieldsById; break;
                case "focus": target = focusFieldsById; break;
                case "sea_alienation": target = seaAlienationFieldsById; break;
                default: target = null; break;
            }
            if (target == null) {
                continue;
            }
            target.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        return new OperationsCatalog(categoryLabelById, modeFieldsById, enemyFieldsById, rewardFieldsById,
                breachFieldsById, focusFieldsById, seaAlienationFieldsById);
    }

    private static String field(Map<String, Map<String, String>> fieldsById, String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    private static List<String> ids(Map<String, Map<String, String>> fieldsById) {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }

    private static List<String> fieldNames(Map<String, Map<String, String>> fieldsById, String id) {
        Map<String, String> fields = fieldsById.get(id);
        if (fields == null) {
            return List.of();
        }
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        return names;
    }

    /** Real UI tab label for {@code category.<id>} (e.g. {@code "operation"} -&gt; "Operations Mode"). */
    public String categoryLabel(String id) {
        return categoryLabelById.get(id);
    }

    public List<String> categoryIds() {
        List<String> out = new ArrayList<>(categoryLabelById.keySet());
        Collections.sort(out);
        return out;
    }

    public String modeName(String id) { return field(modeFieldsById, id, "name"); }
    public String modeDescription(String id) { return field(modeFieldsById, id, "desc"); }
    public List<String> modeIds() { return ids(modeFieldsById); }

    /** Real raw field value for {@code enemies.<enemyId>.<field>} (e.g. {@code "name"}, {@code "desc"},
     * or an ability's {@code "<abilityKey>.title"}/{@code "<abilityKey>.text"}), or null. */
    public String enemyField(String enemyId, String field) { return field(enemyFieldsById, enemyId, field); }
    public String enemyName(String id) { return enemyField(id, "name"); }
    public String enemyDescription(String id) { return enemyField(id, "desc"); }
    public List<String> enemyIds() { return ids(enemyFieldsById); }
    public List<String> enemyFieldNames(String enemyId) { return fieldNames(enemyFieldsById, enemyId); }

    public String rewardDescription(String id) { return field(rewardFieldsById, id, "desc"); }
    public List<String> rewardIds() { return ids(rewardFieldsById); }

    /** Real raw field value for {@code breach.<id>.<field>}, or null. */
    public String breachField(String id, String field) { return field(breachFieldsById, id, field); }
    public String breachName(String id) { return breachField(id, "name"); }
    public String breachDescription(String id) { return breachField(id, "desc"); }
    public List<String> breachIds() { return ids(breachFieldsById); }
    public List<String> breachFieldNames(String id) { return fieldNames(breachFieldsById, id); }

    /** Real raw field value for {@code focus.<id>.<field>}, or null. */
    public String focusField(String id, String field) { return field(focusFieldsById, id, field); }
    public String focusName(String id) { return focusField(id, "name"); }
    public String focusDescription(String id) { return focusField(id, "desc"); }
    public List<String> focusIds() { return ids(focusFieldsById); }
    public List<String> focusFieldNames(String id) { return fieldNames(focusFieldsById, id); }

    /** Real raw field value for {@code sea_alienation.<id>.<field>}, or null. */
    public String seaAlienationField(String id, String field) { return field(seaAlienationFieldsById, id, field); }
    public String seaAlienationName(String id) { return seaAlienationField(id, "name"); }
    public String seaAlienationDescription(String id) { return seaAlienationField(id, "desc"); }
    public List<String> seaAlienationIds() { return ids(seaAlienationFieldsById); }
    public List<String> seaAlienationFieldNames(String id) { return fieldNames(seaAlienationFieldsById, id); }
}

package net.stalzone.emulator.world;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves real short in-game zone codes ({@code mg}/{@code pd}/{@code td}/{@code csky}/{@code
 * spusk}/...) to their real, human-readable display names (part 105) -- the real {@code
 * location.&lt;id&gt;.name} key family inside {@code stalker/lang/en.lang} (part 90's
 * {@link LangCatalog}), the single richest zone-identity resource this project has ever found:
 * 298 real {@code location.<id>.name} entries (a subset of the {@code location} prefix's 345
 * total keys that {@link LangCatalog}'s own class doc already counted but never itself parsed
 * out into {@code .name} identity lines specifically -- this class is the first to do that).
 *
 * <p>This single namespace RESOLVES several of this project's oldest disclosed open questions,
 * left unidentified for anywhere from 2 to 79 parts:
 * <ul>
 * <li>{@code mg}/{@code pd}/{@code td} -- {@link MilestoneMap}'s own part-26 doc left these three
 * short {@code map_cache} zone codes "confirmed-real-but-unidentified" after finding no
 * client-log text that named them, and part 53 could still only place them "in the exact same
 * reference class as already-confirmed real zones" via a shared barter-icon convention, still
 * without real names. This part resolves all three directly: {@code location.mg.name=The Dead
 * City}, {@code location.pd.name=The Path of Fools} (independently reinforcing part 96's own
 * resolution of {@code pd}, reached a different way), {@code location.td.name=Black Willows}
 * (with a real supporting description: "Due to the constant presence of military stalkers,
 * stalkers prefer not to set up major camps in the Black Willows... a cunningly hidden
 * archianomaly, which often spawns artifacts" -- also directly resolving {@link
 * net.stalzone.emulator.world.props.StalkerClansCatalog}'s part-72 "a real, unexplained
 * short-code lead" note for the same {@code td}).</li>
 * <li>{@code csky} -- {@link SettlementRegistry}'s part-28 doc could only offer an "informed
 * reading" that {@code csky} "plausibly abbreviates... Clear Sky," the third canonical
 * S.T.A.L.K.E.R.-trilogy faction, explicitly flagged as NOT confirmed. {@code
 * settlement.id.csky.title=Ataman's HQ} (a sibling real key family, {@code settlement.id.<id>.
 * title}, also first parsed out this part -- see {@link SettlementRegistry#realTitle}) instead
 * confirms {@code csky} as "Ataman's HQ," home to "the swamp stalkers and their leader, the
 * Ataman" ({@code settlement.id.csky.level.4.desc}) -- the Clear-Sky reading was a reasonable
 * guess from the original trilogy's own faction roster, but is now superseded by this project's
 * own real, literal client text. {@code location.atamans_hq.name} and {@code location.dungeon_
 * atamans_hq.name} independently reuse the exact same real display name for two further,
 * distinct zone ids -- disclosed as the same real place name recurring, not investigated for
 * whether all three are literally one shared zone.</li>
 * <li>{@code spusk} -- part 104's {@code StalkerSoundEventsCatalog} left {@code spusk/} (part of
 * the {@code stalker/sound/events/} ambience roster) in its disclosed-real-but-unconfirmed bucket,
 * no {@code en.lang} match found at the time. {@code location.dungeon_spusk.name=Vagant's Den}
 * resolves it -- and, since {@code dungeon_spusk} was itself already a confirmed real {@code
 * map_cache} zone since {@link MilestoneMap}'s part 26, this also reinforces part 104's own
 * {@code vagant/} boss finding with a real named lair, not just taunt audio.</li>
 * <li>{@code dungeon_school} -- {@code location.dungeon_school.name=Vlasov's School} is a real,
 * previously-uncatalogued named location tying to this project's existing "Colonel Vlasov"
 * narrative thread (part 102's corroboration list).</li>
 * </ul>
 *
 * <p>Further real, disclosed-but-not-individually-investigated-further findings surfaced while
 * building this registry (not exhaustively covered above -- see the real {@code
 * location.<id>.name} data itself for the remaining ~280 entries): {@code location.battleroyale.
 * name} and {@code location.dmz.name} both real-resolve to {@code Boral}, directly matching the
 * place name already seen in part 104's real frontline-presentation text ("data from Boral,
 * Lyubech-3..."); {@code location.rls.name=The Red Forest} names the real Radar Station area
 * code; {@code location.yanov.name=Uranus Mill}; {@code location.poligon2.name=Chernobyl NPP} is
 * a real, major, previously-unnamed zone; {@code location.bar.name=Pervomaisky Factory} and
 * {@code location.bar_save.name=Pervomaisky Factory Bar} clarify a real naming layer this
 * project had not distinguished before -- the zone code {@code bar} names a factory location,
 * while the separate, already-confirmed {@code rostok} settlement id is the actual "Bar" hangout
 * within/near it ({@code settlement.id.rostok.title=The Bar}); {@code location.limansk.name=
 * Lyubech-3}, {@code location.nbolota.name=The Graveyard}, {@code location.yantar.name=The Pit},
 * and {@code location.sovhoz.name=Sovkhoz of Shevchenko} each independently reconfirm parts
 * 93/103/104's own findings through this entirely separate {@code en.lang} key family.
 *
 * <p><b>Honest limits, directly counted against the real data:</b> 68 of the 298 real entries
 * (23%) resolve to the literal value {@code Unknown} -- EXBO's own client marks these zone ids as
 * not-yet-publicly-named, not a gap in this project's own research (e.g. {@code location.
 * angar_s.name=Unknown}); those 68 are still real, disclosed entries (a caller can tell an
 * "Unknown" real placeholder apart from a locationId this registry has never heard of at all,
 * since only the former returns non-null). One entry is self-referential rather than localized:
 * {@code location.dungeon_x18.name=dungeon_x18}. Many display names are legitimately reused
 * across several distinct real zone ids (e.g. "The Path of Fools" names 5 different ids: {@code
 * pd}, {@code inside_pd}, {@code dungeon_barsani}, {@code dungeon_miar2}, {@code dungeon_miar3}
 * -- real sub-instances/entrances of one location, not a data error), so {@link #realName} is
 * not safely invertible from display name back to a single canonical id.
 *
 * <p><b>UPDATED part 110:</b> a separate real {@code en.lang} namespace, {@code loadscreens.
 * location.<id>.name} (see {@link net.stalzone.emulator.world.props.LoadscreenCatalog}), shares
 * much of this registry's id space but is NOT always identical text for the same id -- honestly
 * disclosed there rather than here: {@code poligon2} resolves to "Chernobyl NPP" in this registry
 * but "Reactor 4" in the loadscreens namespace for the same real zone code (plausibly a whole-
 * complex name vs. one building within it, not a contradiction).
 */
public final class LocationNameRegistry {

    private static final String PREFIX = "location.";
    private static final String SUFFIX = ".name";

    private final Map<String, String> nameById;

    private LocationNameRegistry(Map<String, String> nameById) {
        this.nameById = nameById;
    }

    /**
     * @param catalog a loaded {@link LangCatalog} (part 90), e.g. from {@code en.lang}. Scans
     *                every real key matching {@code location.<id>.name} (an id with no further
     *                dots -- guards against ever misreading some other, deeper {@code location.*}
     *                key as if it were an identity line).
     */
    public static LocationNameRegistry load(LangCatalog catalog) {
        Map<String, String> nameById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (!key.endsWith(SUFFIX)) {
                continue;
            }
            String id = key.substring(PREFIX.length(), key.length() - SUFFIX.length());
            if (!id.isEmpty() && id.indexOf('.') < 0) {
                nameById.put(id, catalog.get(key));
            }
        }
        return new LocationNameRegistry(nameById);
    }

    /**
     * The real, human-readable display name for real zone code {@code locationId} (e.g.
     * {@code "td"} -&gt; {@code "Black Willows"}), or {@code null} if this registry has no {@code
     * location.<locationId>.name} entry at all. Returns the literal string {@code "Unknown"} (not
     * null) for one of the 68 real entries EXBO's own client has not yet given a public name --
     * see this class's doc for why that distinction matters.
     */
    public String realName(String locationId) {
        return nameById.get(locationId);
    }

    /** Every real zone code this registry has a {@code location.*.name} entry for, alphabetical. */
    public List<String> locationIds() {
        List<String> ids = new ArrayList<>(nameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    /** Total real {@code location.<id>.name} entries loaded (298 against the real, full en.lang). */
    public int size() {
        return nameById.size();
    }
}

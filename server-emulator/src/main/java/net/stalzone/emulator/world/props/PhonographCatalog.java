package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 135-line {@code phonograph.*} namespace in {@code stalker/lang/en.lang} (part
 * 130, the eighth concrete namespace built from part 123's follow-up census) -- a real in-game
 * "phonograph"/jukebox collectible-vinyl-and-soundtrack feature.
 *
 * <p>Confirmed by direct count to split into two real groups with no leftover lines (100 + 35 =
 * 135): {@code gui.track.<trackId>} (100 lines, flat, real soundtrack TRACK NAMES playable through
 * the in-game phonograph -- a mix of real named vocal tracks, e.g. {@code
 * Grin_Danilov_moi_dozhd}="My Rain", {@code Grin_Danilov_na_reke}="On The River", and real ambient
 * ("Sounds of...") tracks tied to already-confirmed real locations, e.g. {@code
 * ambient_bolota}="Sounds of the Swamps", {@code ambient_limansk}="Sounds of the Lyubech-3"),
 * {@code vinyl_<category>.title} (35 lines, flat, real COLLECTIBLE VINYL RECORD titles): most
 * ids fall into two real category prefixes, {@code vinyl_location_<zoneId>} (per-location
 * collectible records reusing already-confirmed real location display names, e.g. {@code
 * vinyl_location_bolota}="The Swamps") and {@code vinyl_event_<eventId>} (event-tied collectible
 * records: {@code black_raid}, {@code blizzard}, {@code dmz26}, {@code frontline}, {@code
 * newyear26}, {@code reichstag_breakthrough} -- another entry in this project's long-running
 * Reichstag corroboration chain, no new ordinal asserted -- {@code rus_arena}), plus one real
 * standalone id outside either category, {@code vinyl_gribnaya_bylina}.
 *
 * <p>This class stores the {@code vinyl_*} id verbatim (including its {@code vinyl_} prefix and
 * category infix) rather than splitting it into a category/sub-id pair, since the one real
 * standalone id ({@code vinyl_gribnaya_bylina}) does not fit either category cleanly and this
 * project does not invent a taxonomy the client data itself does not enforce.
 */
public final class PhonographCatalog {

    private final Map<String, String> trackTitleById;
    private final Map<String, String> vinylTitleById;

    private PhonographCatalog(Map<String, String> trackTitleById, Map<String, String> vinylTitleById) {
        this.trackTitleById = trackTitleById;
        this.vinylTitleById = vinylTitleById;
    }

    private static final String TRACK_PREFIX = "phonograph.gui.track.";
    private static final String VINYL_PREFIX = "phonograph.";
    private static final String VINYL_TITLE_SUFFIX = ".title";
    private static final String VINYL_ID_MARKER = "vinyl";

    public static PhonographCatalog load(LangCatalog catalog) {
        Map<String, String> trackTitleById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(TRACK_PREFIX)) {
            trackTitleById.put(key.substring(TRACK_PREFIX.length()), catalog.get(key));
        }

        Map<String, String> vinylTitleById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(VINYL_PREFIX)) {
            String rest = key.substring(VINYL_PREFIX.length());
            if (rest.startsWith(VINYL_ID_MARKER) && rest.endsWith(VINYL_TITLE_SUFFIX)) {
                String id = rest.substring(0, rest.length() - VINYL_TITLE_SUFFIX.length());
                vinylTitleById.put(id, catalog.get(key));
            }
        }

        return new PhonographCatalog(trackTitleById, vinylTitleById);
    }

    public String trackTitle(String trackId) { return trackTitleById.get(trackId); }

    /** Real track ids, sorted. */
    public List<String> trackIds() {
        List<String> out = new ArrayList<>(trackTitleById.keySet());
        Collections.sort(out);
        return out;
    }

    /** The real vinyl title for this id, e.g. {@code "vinyl_location_bolota"} or {@code "vinyl_gribnaya_bylina"}. */
    public String vinylTitle(String vinylId) { return vinylTitleById.get(vinylId); }

    /** Real vinyl ids (verbatim, including their {@code vinyl_} prefix), sorted. */
    public List<String> vinylIds() {
        List<String> out = new ArrayList<>(vinylTitleById.keySet());
        Collections.sort(out);
        return out;
    }
}

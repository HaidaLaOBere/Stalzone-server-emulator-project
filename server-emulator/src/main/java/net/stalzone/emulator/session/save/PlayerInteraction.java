package net.stalzone.emulator.session.save;

import java.util.Map;

/**
 * One entry from a real per-character save file's top-level {@code interacts} list (see
 * {@link PlayerSaveFile} for where that list lives and how it's loaded). Each entry records one
 * player-vs-player or player-vs-vendor event involving the OTHER STALKER's real account name,
 * so -- same discipline this project already applies to real dialogue text and real player-owned
 * data elsewhere -- that name is treated as personal data belonging to a real third party and is
 * never hardcoded or logged by this class beyond exposing the field for the loading server's own
 * use. Nothing in this codebase's docs/comments/tests cites an actual username.
 *
 * <p>Real schema, confirmed from two real characters' {@code sd1} files (1 entry + 29 entries,
 * different accounts):
 * <pre>
 *   {type=DIED_FROM_PLAYER, username=&lt;other player's real name&gt;, allianceId=freedom}
 * </pre>
 * All three fields are plain JSON/NBT strings. {@code allianceId} is the OTHER player's faction,
 * using the pre-EXBO-rename internal ids this project already confirmed elsewhere (part 36):
 * {@code freedom}, {@code duty}, {@code stalkers}, {@code bandits}, {@code merc}, {@code covenant}.
 *
 * <p><b>{@code type} is deliberately NOT a closed enum here.</b> Only 6 distinct values have been
 * observed across the ~30 real entries seen (2 characters is a small sample): {@code
 * DIED_FROM_PLAYER}, {@code PLAYER_KILLED}, {@code REVIVED_BY_PLAYER}, {@code REVIVED_PLAYER},
 * {@code BOUGHT_LOT_FROM}, {@code SOLD_LOT_TO}. Their names are self-explanatory pairs (you died to
 * them / you killed them, they revived you / you revived them, you bought a market lot from them /
 * you sold one to them), but this project's rule 1 forbids treating "6 observed values" as "the
 * complete set" without more evidence, so {@link #type()} stays a raw string rather than a Java
 * enum that would silently misrepresent an unseen 7th value.
 */
public final class PlayerInteraction {

    private final Map<String, Object> fields;

    PlayerInteraction(Map<String, Object> fields) {
        this.fields = fields;
    }

    /** Direct access to every field this entry has, exactly as parsed from NBT. */
    public Map<String, Object> rawFields() {
        return fields;
    }

    /**
     * Raw interaction-kind string. 6 values confirmed so far (see class doc); not a closed set.
     */
    public String type() {
        return stringField("type");
    }

    /** The other player's real account name. See this class's doc for the privacy handling around this field. */
    public String username() {
        return stringField("username");
    }

    /** The other player's faction id at the time of the interaction (internal id, see class doc). */
    public String allianceId() {
        return stringField("allianceId");
    }

    private String stringField(String key) {
        Object v = fields.get(key);
        return v instanceof String ? (String) v : null;
    }

    @Override
    public String toString() {
        return "PlayerInteraction{type=" + type() + ", allianceId=" + allianceId() + "}";
    }
}

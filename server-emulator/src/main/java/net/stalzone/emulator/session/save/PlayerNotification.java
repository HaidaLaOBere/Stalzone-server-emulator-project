package net.stalzone.emulator.session.save;

import java.util.Map;

/**
 * One entry from a real per-character save file's top-level {@code notifications} list (see
 * {@link PlayerSaveFile}). This is the client's in-game notification/toast log -- quest
 * progress, world-event pings, mail, etc. Confirmed from one real character's real {@code sd2}
 * file: 60 entries across 6 distinct {@code channel_id} values.
 *
 * <p>Real outer shape, confirmed field-by-field via direct NBT-type inspection (not guessed from
 * the pretty-printer, which caps list previews at 3 items -- see {@code NbtDumpTool}'s doc):
 * <pre>
 *   { notification: { channel_id: String, time: Long, expire_time: Long, payload: Map },
 *     isRead: Byte, receivedMoment: Long }
 * </pre>
 *
 * <p><b>{@code payload}'s shape depends on {@code channel_id}</b> -- this is a real, confirmed
 * per-channel variant structure, not one fixed schema:
 * <ul>
 *   <li>{@code quests} (36 of 60 real entries): {@code QuestId} (Integer, present on {@code new}/
 *       {@code quest_finished}, ABSENT on {@code quest_failed}/{@code mail_reward}),
 *       {@code type} (String: {@code new}/{@code quest_finished}/{@code quest_failed}/
 *       {@code mail_reward} -- all 4 confirmed real), {@code QuestTitle} (String, real quest
 *       names e.g. "For the Good of the Mercenaries", "An Important Delivery"), {@code quest_type}
 *       (Integer: only {@code 0} and {@code 2} seen -- meaning not confirmed from this data alone,
 *       though {@code quest_type=2} entries are the only ones seen cycling
 *       new-&gt;failed-&gt;new-&gt;failed-&gt;...-&gt;finished, suggestive of a repeatable/bounty
 *       quest class vs. {@code quest_type=0}'s single new-&gt;finished progressions -- NOT stated
 *       as confirmed, just noted as a pattern in the one real sample seen).</li>
 *   <li>{@code outpost} (2 of 60): {@code x}, {@code z} (both real JSON/NBT Floats, real world
 *       coordinates near the same location both times -- consistent with "you were pushed out of /
 *       captured an outpost near here", not confirmed against the outpost-capture mechanic
 *       described in part 36's official patch notes, just structurally plausible).</li>
 *   <li>{@code handbook} (6 of 60): {@code link} (String, real values like
 *       {@code handbook://articles/pvp/smert.md/#header_3} -- the article-path categories seen
 *       (pvp, stats, progression, arthunt) line up with topic areas this project has separately
 *       confirmed exist in-game, e.g. arthunter enemies (part 33) and outposts (part 36)).</li>
 *   <li>{@code generic} (14 of 60): heterogeneous -- battle-pass/challenge-completion toasts
 *       ({@code msg}/{@code title} as localization-key maps, {@code link} as a
 *       {@code seasonpass://...} URI) and at least one plain world-event ping with only
 *       {@code msg}. Real localization keys seen include {@code challenges.triumph.triumph2025},
 *       {@code challenges.triumph.triumph2026}, and {@code challenges.season.summer26} --
 *       confirming this project's part-36 note that STALZONE runs year-dated seasonal
 *       battle-pass content, from at least one real save file, not just patch-notes text.</li>
 *   <li>{@code savepoint} (1 of 60), {@code mail} (1 of 60): seen once each in this sample; too
 *       thin an evidence base for a typed accessor, exposed only via {@link #payload()}.</li>
 * </ul>
 *
 * <p>Per this project's rule 1, typed convenience accessors below are provided ONLY for the three
 * well-evidenced channels (quests, outpost, handbook); {@code generic}/{@code savepoint}/
 * {@code mail} are reachable only through the raw {@link #payload()} map.
 */
public final class PlayerNotification {

    private final Map<String, Object> fields;
    private final Map<String, Object> notification;
    private final Map<String, Object> payload;

    @SuppressWarnings("unchecked")
    PlayerNotification(Map<String, Object> fields) {
        this.fields = fields;
        Object n = fields.get("notification");
        this.notification = (n instanceof Map) ? (Map<String, Object>) n : Map.of();
        Object p = notification.get("payload");
        this.payload = (p instanceof Map) ? (Map<String, Object>) p : Map.of();
    }

    /** Direct access to this entry's outer fields (notification/isRead/receivedMoment), exactly as parsed. */
    public Map<String, Object> rawFields() {
        return fields;
    }

    /** Direct access to the inner {@code notification} map's fields. */
    public Map<String, Object> notificationFields() {
        return notification;
    }

    /** Direct access to the channel-specific payload map, whatever shape it has for this entry's channel. */
    public Map<String, Object> payload() {
        return payload;
    }

    public boolean isRead() {
        Object v = fields.get("isRead");
        return (v instanceof Byte) && ((Byte) v) != 0;
    }

    public Long receivedMoment() {
        return longField(fields, "receivedMoment");
    }

    public String channelId() {
        Object v = notification.get("channel_id");
        return v instanceof String ? (String) v : null;
    }

    public Long time() {
        return longField(notification, "time");
    }

    public Long expireTime() {
        return longField(notification, "expire_time");
    }

    // --- quests channel ---

    public boolean isQuestChannel() {
        return "quests".equals(channelId());
    }

    /** Null on {@code quest_failed}/{@code mail_reward} entries -- see class doc, this is real, not a bug. */
    public Integer questId() {
        return intField(payload, "QuestId");
    }

    public String questTitle() {
        Object v = payload.get("QuestTitle");
        return v instanceof String ? (String) v : null;
    }

    /** The payload's {@code type} field: {@code new}/{@code quest_finished}/{@code quest_failed}/{@code mail_reward}. */
    public String questNotificationType() {
        Object v = payload.get("type");
        return v instanceof String ? (String) v : null;
    }

    /** Raw {@code quest_type} code (only 0 and 2 confirmed seen); meaning not officially confirmed, see class doc. */
    public Integer questTypeCode() {
        return intField(payload, "quest_type");
    }

    // --- outpost channel ---

    public boolean isOutpostChannel() {
        return "outpost".equals(channelId());
    }

    public Float outpostX() {
        return floatField(payload, "x");
    }

    public Float outpostZ() {
        return floatField(payload, "z");
    }

    // --- handbook channel ---

    public boolean isHandbookChannel() {
        return "handbook".equals(channelId());
    }

    public String handbookLink() {
        Object v = payload.get("link");
        return v instanceof String ? (String) v : null;
    }

    private static Long longField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Long ? (Long) v : null;
    }

    private static Integer intField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Integer ? (Integer) v : null;
    }

    private static Float floatField(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Float ? (Float) v : null;
    }

    @Override
    public String toString() {
        return "PlayerNotification{channel=" + channelId() + ", payload=" + payload + "}";
    }
}

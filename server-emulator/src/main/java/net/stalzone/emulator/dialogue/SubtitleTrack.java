package net.stalzone.emulator.dialogue;

import java.util.Collections;
import java.util.List;

/**
 * The full cue list for one real {@code <npcId>.srt} file in one locale -- e.g.
 * {@code bigeagle_bar} in {@code en}. See {@link SubtitleRegistry} for how these are discovered
 * and loaded, and docs/RESEARCH_LOG.md part 34 for the story-arc catalogue this project built
 * from the real {@code npcId} roster (108 files: duty/merc/stalkers/zarya/zavet/palach/bigeagle/
 * twins/nina/skalolaz/etc.).
 *
 * <p><b>Real, confirmed cross-locale fact</b>: for every file spot-checked (part 34 and this
 * class's own load-time consistency check, see {@link SubtitleRegistry#load}), the cue
 * TIMESTAMPS are byte-identical across all 5 real locale folders ({@code en/es/fr/ko/ru}) for
 * the same {@code npcId} -- only the {@code text} differs. That means these are localized
 * text tracks over one shared, fixed audio timeline, not independently-timed translations --
 * a real fact about how the client's localization pipeline works, not a guess.
 */
public final class SubtitleTrack {

    private final String npcId;
    private final String locale;
    private final List<SubtitleCue> cues;

    public SubtitleTrack(String npcId, String locale, List<SubtitleCue> cues) {
        this.npcId = npcId;
        this.locale = locale;
        this.cues = Collections.unmodifiableList(cues);
    }

    public String npcId() {
        return npcId;
    }

    public String locale() {
        return locale;
    }

    public List<SubtitleCue> cues() {
        return cues;
    }

    /** Last cue's end time -- an honest lower bound on the real audio clip's length, not a guess. */
    public long totalDurationMs() {
        if (cues.isEmpty()) {
            return 0L;
        }
        return cues.get(cues.size() - 1).endMs();
    }

    public int cueCount() {
        return cues.size();
    }
}

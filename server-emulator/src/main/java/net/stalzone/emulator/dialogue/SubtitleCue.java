package net.stalzone.emulator.dialogue;

/**
 * One timed line from a real {@code .srt} subtitle file (see {@link SubtitleTrack}). Plain,
 * standard SRT cue shape: a 1-based {@code index}, a {@code startMs}/{@code endMs} time range
 * (parsed from the file's own {@code HH:MM:SS,mmm --> HH:MM:SS,mmm} line), and the cue's text.
 *
 * <p>Every real file checked across this project (108 files x 5 locales, see
 * docs/RESEARCH_LOG.md part 34) has exactly one text line per cue -- {@link SubtitleRegistry}
 * would throw while loading a file that broke that pattern rather than silently dropping extra
 * lines, so the single-{@code text}-field shape here isn't an unverified assumption.
 */
public final class SubtitleCue {

    private final int index;
    private final long startMs;
    private final long endMs;
    private final String text;

    public SubtitleCue(int index, long startMs, long endMs, String text) {
        this.index = index;
        this.startMs = startMs;
        this.endMs = endMs;
        this.text = text;
    }

    public int index() {
        return index;
    }

    public long startMs() {
        return startMs;
    }

    public long endMs() {
        return endMs;
    }

    public long durationMs() {
        return endMs - startMs;
    }

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return "#" + index + " [" + startMs + "-" + endMs + "ms] " + text;
    }
}

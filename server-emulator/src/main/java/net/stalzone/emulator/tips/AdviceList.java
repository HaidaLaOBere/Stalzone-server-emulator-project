package net.stalzone.emulator.tips;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads one real {@code modassets/assets/gloomycore/advices_<locale>.txt} file -- real,
 * player-facing loading-tip/advice text (part 51), CRLF-terminated, one tip per paragraph,
 * paragraphs separated by a blank line (a bare {@code \r\n\r\n}), no trailing newline after the
 * last tip. Confirmed real content by direct inspection: the English copy
 * ({@code advices_en.txt}, 6 tips) covers ordinary gameplay tips ({@code "To view the active
 * quest, press «K»."}, a chat-etiquette notice, a {@code '/suicide'} command mention);
 * the Russian copy ({@code advices_ru.txt}, 84 tips -- a much larger, mostly DIFFERENT set, not
 * just a translation of the English 6) is almost entirely real account-security/support notices
 * attributed to {@code © Техническая
 * поддержка} ("Technical Support") and
 * {@code © Администрация}
 * ("Administration"), repeatedly naming the real domains {@code support.exbo.ru} and
 * {@code forum.exbo.ru} -- real, direct evidence (independent of any prior part) of this
 * project's already-established EXBO/support-infrastructure findings, and also the string
 * {@code "STALCRAFT"} itself (the English copy's community-hub tip), corroborating this
 * project's existing evidence that STALZONE was previously/is-also known by that name.
 *
 * <p>Content-catalog only, same category as {@code dialogue/SubtitleRegistry}: nothing in this
 * project's evidence shows what actually triggers a given tip to display in a live client (no
 * "which tip shows on which loading screen" logic found anywhere), so this class does not invent
 * a selection/rotation policy -- {@link #tips()} exposes the real strings in real file order and
 * nothing more.
 */
public final class AdviceList {

    private final List<String> tips;

    private AdviceList(List<String> tips) {
        this.tips = tips;
    }

    public static AdviceList load(Path advicesTxtPath) throws IOException {
        String text = Files.readString(advicesTxtPath, StandardCharsets.UTF_8);
        List<String> tips = new ArrayList<>();
        for (String rawParagraph : text.split("\r\n\r\n|\n\n")) {
            String tip = rawParagraph.replace("\r\n", "\n").trim();
            if (!tip.isEmpty()) {
                tips.add(tip);
            }
        }
        if (tips.isEmpty()) {
            throw new IOException(advicesTxtPath + ": no tip text found");
        }
        return new AdviceList(tips);
    }

    /** Every real tip string in this locale's file, in file order. */
    public List<String> tips() {
        return Collections.unmodifiableList(tips);
    }

    public int size() {
        return tips.size();
    }
}

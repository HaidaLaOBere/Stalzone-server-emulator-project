package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs the real 38-line {@code premium.*} namespace in {@code stalker/lang/en.lang} (part
 * 143, the twenty-third concrete namespace built from part 123's follow-up census) -- real
 * "STALZONE PREMIUM" subscription-perk marketing copy, modeled with the established
 * generic-table-plus-flat-carve-out shape (same pattern as {@link PartyMissionsCatalog}'s lone
 * {@code name_format} exception): a generic last-dot-split (id, field) table for every real line
 * with at least one dot after the prefix, plus one separate flat {@link #desc} field for the one
 * real zero-dot line, {@code premium.desc}.
 *
 * <p><b>Real shape, reconciled exactly: 1 + 3 + 12 + 2 + 20 = 38.</b> {@code premium.desc} (1
 * line) is the top banner line. {@code premium.inline_perk.<field>} (3 lines: {@code barter}/
 * {@code filler}/{@code other}) is a short 3-bullet summary. {@code
 * premium.perk_block.<id>.title}/{@code .description} (12 lines, 6 ids: {@code barter}/{@code
 * fillers}/{@code reputation}/{@code quests}/{@code daily_rewards}/{@code personal_storage}) is
 * an OLDER, numbered (literally "1."-"6." in the title text) perk-detail layout. {@code
 * premium.purchaseOptions.<id>.label} (2 lines: {@code best}="BEST OFFER", {@code
 * optimal}="OPTIMAL OFFER") is real purchase-tier badge text. {@code
 * premium.perkBlocks.<id>.title}/{@code .desc}/{@code .details.<n>} (20 lines, 6 ids: {@code
 * barter}/{@code loot}/{@code reputation}/{@code quest}/{@code weekly_bonus}/{@code all}) is a
 * NEWER, differently-cased UI layout covering the same 6 real perks with all-caps title text.
 * The real {@code .details.<n>} nested field lands as id={@code perkBlocks.<perkId>.details},
 * field={@code <n>} under the generic split -- a literal digit field name, same handling already
 * used for part 140's {@code daily_bonus.description.summer25.1}, no special-casing needed.
 *
 * <p>Both perk layouts cover the same real 6 perks under different id spellings/casings (e.g.
 * {@code perk_block.fillers} vs {@code perkBlocks.loot} both describe the same "earn more
 * resources for sale" perk) -- disclosed as the client shipping two UI generations' worth of
 * copy for the same real feature set side by side, not merged into one id space. The real
 * {@code §#FFC34F...§r} inline color-tag markup in the older layout's descriptions is preserved
 * verbatim (a real client rich-text convention, not an encoding error).
 */
public final class PremiumCatalog {

    private final String desc;
    private final Map<String, Map<String, String>> fieldsById;

    private PremiumCatalog(String desc, Map<String, Map<String, String>> fieldsById) {
        this.desc = desc;
        this.fieldsById = fieldsById;
    }

    private static final String PREFIX = "premium.";
    private static final String DESC_KEY = PREFIX + "desc";

    public static PremiumCatalog load(LangCatalog catalog) {
        String desc = catalog.get(DESC_KEY);
        Map<String, Map<String, String>> fieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(PREFIX)) {
            if (key.equals(DESC_KEY)) {
                continue;
            }
            String rest = key.substring(PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            fieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }
        return new PremiumCatalog(desc, fieldsById);
    }

    /** The one real flat top-banner line, {@code premium.desc}. */
    public String desc() {
        return desc;
    }

    public String field(String id, String field) {
        Map<String, String> fields = fieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** Real ids with at least one field, sorted. */
    public List<String> ids() {
        List<String> out = new ArrayList<>(fieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}

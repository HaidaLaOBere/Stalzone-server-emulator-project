package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogs three real tiny namespaces from {@code stalker/lang/en.lang} together (part 161, the
 * forty-fourth/forty-fifth/forty-sixth concrete namespaces built from part 123's follow-up
 * census), kept as separate tables in one class: two real one-off in-game economy-change
 * announcement banners and the real barter-category labels one of them describes.
 *
 * <p><b>{@code banner.information.barter_upgrade_compensation.*}</b> (9 lines) is a real,
 * deeply-nested one-off patch-notification banner announcing a "BARTER AND MODIFICATIONS REWORK"
 * -- modeled with the established generic last-dot-split (id, field) table, which naturally
 * yields 6 distinct real ids from the nesting with no special-casing needed: the base id (2
 * fields: {@code end_time_line}, {@code title}), plus two numbered {@code info_block.&lt;n&gt;}
 * ids (2 fields each: {@code title}, {@code description}) each with their own nested {@code
 * link.&lt;n&gt;.name} id(s) (1-2 fields). Real content: block 1 ("CHANGES") announces the barter/
 * modification mechanic changes; block 2 ("COMPENSATION") explains gear-conversion compensation.
 *
 * <p><b>{@code barter.category.&lt;id&gt;}</b> (2 lines, flat PREFIX strip) names the real 2
 * barter UI categories this banner's rework affected: {@code armor}="Suits", {@code
 * modules}="Attachments" -- a direct real thematic pairing with the banner above, disclosed as a
 * link only, no shared id/key structure ties the two namespaces together.
 *
 * <p><b>{@code main_menu_popup.act1_ticket_warning.*}</b> (2 lines, one real id) is a real,
 * separate one-off economy-lifecycle announcement: "ACT TICKETS REMOVAL — JUNE 4", warning that
 * Act 1 Tickets convert to trade coins (1 ticket = 50 coins) and that two real named cosmetic
 * items ("ZIVCAS ArcticSafe-6 Container", "ZIVCAS Valor Dagger" -- real {@code ZIVCAS} branding,
 * already confirmed as the in-fiction weapons corporation by part 115) leave the shop on that
 * date. Real embedded {@code &#167;#RRGGBB} client color-markup tokens are preserved verbatim in
 * the field value, not stripped.
 */
public final class EconomyRolloutCatalog {

    private final Map<String, Map<String, String>> bannerFieldsById;
    private final Map<String, String> barterCategoryById;
    private final Map<String, String> act1TicketWarningFields;

    private EconomyRolloutCatalog(Map<String, Map<String, String>> bannerFieldsById, Map<String, String> barterCategoryById,
            Map<String, String> act1TicketWarningFields) {
        this.bannerFieldsById = bannerFieldsById;
        this.barterCategoryById = barterCategoryById;
        this.act1TicketWarningFields = act1TicketWarningFields;
    }

    private static final String BANNER_PREFIX = "banner.";
    private static final String BARTER_CATEGORY_PREFIX = "barter.category.";
    private static final String ACT1_TICKET_WARNING_PREFIX = "main_menu_popup.act1_ticket_warning.";

    public static EconomyRolloutCatalog load(LangCatalog catalog) {
        Map<String, Map<String, String>> bannerFieldsById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(BANNER_PREFIX)) {
            String rest = key.substring(BANNER_PREFIX.length());
            int lastDot = rest.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }
            String id = rest.substring(0, lastDot);
            String field = rest.substring(lastDot + 1);
            bannerFieldsById.computeIfAbsent(id, k -> new LinkedHashMap<>()).put(field, catalog.get(key));
        }

        Map<String, String> barterCategoryById = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(BARTER_CATEGORY_PREFIX)) {
            String id = key.substring(BARTER_CATEGORY_PREFIX.length());
            barterCategoryById.put(id, catalog.get(key));
        }

        Map<String, String> act1TicketWarningFields = new LinkedHashMap<>();
        for (String key : catalog.keysWithPrefix(ACT1_TICKET_WARNING_PREFIX)) {
            String field = key.substring(ACT1_TICKET_WARNING_PREFIX.length());
            act1TicketWarningFields.put(field, catalog.get(key));
        }

        return new EconomyRolloutCatalog(bannerFieldsById, barterCategoryById, act1TicketWarningFields);
    }

    public String bannerField(String id, String field) {
        Map<String, String> fields = bannerFieldsById.get(id);
        return fields == null ? null : fields.get(field);
    }

    /** A real barter UI category's display name, e.g. {@code armor} -&gt; "Suits". */
    public String barterCategory(String id) {
        return barterCategoryById.get(id);
    }

    /** A real Act 1 Ticket warning field ({@code title}/{@code text}). */
    public String act1TicketWarningField(String field) {
        return act1TicketWarningFields.get(field);
    }

    /** Real banner ids with at least one field, sorted. */
    public List<String> bannerIds() {
        List<String> out = new ArrayList<>(bannerFieldsById.keySet());
        Collections.sort(out);
        return out;
    }
}

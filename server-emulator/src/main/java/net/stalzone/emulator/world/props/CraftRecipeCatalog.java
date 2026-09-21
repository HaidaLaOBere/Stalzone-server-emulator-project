package net.stalzone.emulator.world.props;

import net.stalzone.emulator.localization.LangCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real crafting-system data from {@code stalker/lang/en.lang}'s {@code craft_recipe} namespace (49
 * lines total; part 90's {@link LangCatalog} counted it but explicitly flagged it "an unconfirmed
 * crafting-system lead, not investigated" -- this class is that investigation, part 106).
 *
 * <p>Confirms a real crafting system distinct from {@code world/props/HideoutCatalog}'s workbench
 * skill/feature roster (that class covers the crafting STATIONS and unlocks; this class covers the
 * actual RECIPES craftable at them). Five real recipe groups ({@code craft_recipe.group.<id>}):
 * {@code food}="Food", {@code containers}="Containers", {@code other}="Other", {@code ammo}=
 * "Rounds", {@code med}="Medicine".
 *
 * <p>22 real recipe ids ({@code craft_recipe.id.<id>.name}/{@code .desc}, not every id has a real
 * {@code .desc} line): two duplicate-named sandwiches ({@code sandwich_white}="Salami Sandwich" on
 * white bread, {@code sandwich_black}="Salami Sandwich" on rye bread -- a real case, like part
 * 105's {@code location.*} findings, of two distinct real ids sharing one display name) and {@code
 * sandwich_caviar}="Caviar Sandwich"; a KZS container-combining chain ({@code 2_kzs_1} through
 * {@code 5_kzs_1}, and a {@code _1u} variant chain, real real text: "A spacious container based on
 * KZS-1 technology"); {@code accum_old}/{@code accum}="Bell Battery" (both real, charged "using a
 * Spectral Crystal" -- a real anomalous-power-source item this project has not otherwise
 * catalogued); {@code gaus_ammo}="Fragment Projectiles for Gauss Rifle" (confirms a real craftable
 * Gauss Rifle ammunition/battery combo); {@code homut_ris}/{@code homut_ris_double}=real
 * "Clamp with RIS Rail" weapon-attachment recipes; and six real alchemy/medicine recipes crafted
 * from mutant parts and Zone flora -- {@code ointment_artefact}="Stone Skin Ointment" (mixed
 * inexpensive artifacts), {@code mozgovik}="Brainer" (rat brains + cerebral hyacinth, reduces
 * "sensitivity to psyonic"), {@code ointment_dogs}="Doggy Secret from Mutt Tails" (ground mutt
 * tails + blue L-chem), {@code ointment_pseudodog}="Doggy Secret from Wild Hound Tails" (wild hound
 * tails + blue L-chem), {@code antirad}="Anti-Radiation Serum" (boar hooves + primrose), {@code
 * ointment_flesh}="Butcher Blend" (piggy's eyes + fire poppies), {@code ointment_snork}="Membrane
 * Oil" (skitter heels + acid webcaps) -- each real ingredient name (mutt/wild hound tails, boar
 * hooves, piggy's eyes, skitter heels, primrose, fire poppies, acid webcaps, cerebral hyacinth) is
 * new, previously-uncatalogued real Zone flora/fauna-derived crafting material text.
 *
 * <p>Live-verified directly against the real client's {@code en.lang} on 2026-09-11; not decoded
 * from any binary recipe-definition format -- only the display layer is confirmed here.
 */
public final class CraftRecipeCatalog {

    private static final String GROUP_PREFIX = "craft_recipe.group.";
    private static final String ID_PREFIX = "craft_recipe.id.";
    private static final String NAME_SUFFIX = ".name";
    private static final String DESC_SUFFIX = ".desc";

    private final Map<String, String> groupNameById;
    private final Map<String, String> recipeNameById;
    private final Map<String, String> recipeDescriptionById;

    private CraftRecipeCatalog(Map<String, String> groupNameById,
                                Map<String, String> recipeNameById,
                                Map<String, String> recipeDescriptionById) {
        this.groupNameById = groupNameById;
        this.recipeNameById = recipeNameById;
        this.recipeDescriptionById = recipeDescriptionById;
    }

    public static CraftRecipeCatalog load(LangCatalog catalog) {
        Map<String, String> groupNameById = new LinkedHashMap<>();
        Map<String, String> recipeNameById = new LinkedHashMap<>();
        Map<String, String> recipeDescriptionById = new LinkedHashMap<>();

        for (String key : catalog.keysWithPrefix(GROUP_PREFIX)) {
            String id = key.substring(GROUP_PREFIX.length());
            groupNameById.put(id, catalog.get(key));
        }

        for (String key : catalog.keysWithPrefix(ID_PREFIX)) {
            String rest = key.substring(ID_PREFIX.length());
            if (rest.endsWith(NAME_SUFFIX)) {
                String id = rest.substring(0, rest.length() - NAME_SUFFIX.length());
                recipeNameById.put(id, catalog.get(key));
            } else if (rest.endsWith(DESC_SUFFIX)) {
                String id = rest.substring(0, rest.length() - DESC_SUFFIX.length());
                recipeDescriptionById.put(id, catalog.get(key));
            }
        }

        return new CraftRecipeCatalog(groupNameById, recipeNameById, recipeDescriptionById);
    }

    public String groupName(String groupId) {
        return groupNameById.get(groupId);
    }

    public String recipeName(String recipeId) {
        return recipeNameById.get(recipeId);
    }

    /** The recipe's real flavor-text description, or {@code null} if this id has none (e.g. {@code homut_ris_double}). */
    public String recipeDescription(String recipeId) {
        return recipeDescriptionById.get(recipeId);
    }

    public List<String> groupIds() {
        List<String> ids = new ArrayList<>(groupNameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public List<String> recipeIds() {
        List<String> ids = new ArrayList<>(recipeNameById.keySet());
        Collections.sort(ids);
        return ids;
    }

    public int recipeCount() {
        return recipeNameById.size();
    }

    public int groupCount() {
        return groupNameById.size();
    }
}

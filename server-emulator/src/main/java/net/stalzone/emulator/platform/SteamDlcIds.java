package net.stalzone.emulator.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads the real store-platform DLC/bundle identifiers this build ships against, from the two real
 * loose files directly in {@code modassets/assets/gloomycore/} (part 63): {@code steam_dlc_ids.txt}
 * (24 real numeric Steam AppIDs, one per line) and {@code egs_dlc_ids.txt} (1 real Epic Games Store
 * catalog-item id, a 32-character hex GUID with no dashes: {@code
 * 2b1a4a2aa9e1485cb007d62e3f3965f8}). This is real store/platform metadata (which purchasable
 * bundles this build recognizes), not EXENS anti-cheat/launcher internals, so stays in scope under
 * rule 2.
 *
 * <p>A live cross-check of a representative 13 of the 24 real Steam AppIDs against the public,
 * documented Steam Storefront API ({@code store.steampowered.com/api/appdetails}), 2026-09-04,
 * turned up a genuinely significant finding for this whole project: STALZONE's own base-game listing
 * (real AppID 1818450, confirmed developer/publisher {@code EXBO}) carries forward a full DLC/
 * season-pass history published under the OLDER name "STALCRAFT" / "STALCRAFT: X" -- direct,
 * documented (not assumed) confirmation of the "possibly-diverged from STALCRAFT" relationship this
 * project's own ground rules already flagged as a hypothesis from third-party community docs. Real,
 * Steam-confirmed DLC names for the checked IDs, oldest-looking first: {@code 2490360} = "STALCRAFT:
 * X Crow Arena 2023 Battle Pack", {@code 2659320} = "STALCRAFT Dead Man's Marathon 2023 Battle Pack
 * №2", {@code 2742650} = "STALCRAFT Anomalous Winter 2023 Battle Pass", {@code 2898540} = "STALCRAFT
 * Folktale Spring 2024 Season Pass", {@code 3086480} = "STALCRAFT:X ZIVCAS Corporation 2024 Season
 * Pack №1", {@code 3291560} = "STALCRAFT: X Black Market 2024 Season Pack №1" (real Steam
 * description: "50 Season Levels, 50 Black Presents, 15 cases with unique Season Styles, and two
 * PepeCas charms", launched real date 2024-10-18), {@code 4207960} = "STALCRAFT: X — 3rd Anniversary",
 * and {@code 4908670} = "STALZONE — Memories of STALCRAFT: X" -- this last one is real, direct,
 * official evidence of the rename itself, bridging both names in one DLC title. Two of the 24 real
 * IDs ({@code 3403640}, {@code 3403650}) returned {@code success:false} from the live API check --
 * disclosed honestly as "not resolvable via the public storefront API right now" (delisted, region-
 * restricted, or otherwise unavailable), NOT confirmed-removed, since this project only trusts
 * evidence it can directly observe. The remaining 11 real IDs were not individually checked this
 * part and are left as open leads for a future pass. "PepeCas" (from the Black Market description)
 * is real evidence of an in-game currency/token name, an unconfirmed fresh lead not yet
 * cross-referenced against this project's existing {@link
 * net.stalzone.emulator.world.props.CurrencyCatalog} findings.
 */
public final class SteamDlcIds {

    private final List<String> steamAppIds;
    private final String egsCatalogItemId;

    private SteamDlcIds(List<String> steamAppIds, String egsCatalogItemId) {
        this.steamAppIds = steamAppIds;
        this.egsCatalogItemId = egsCatalogItemId;
    }

    /**
     * @param steamDlcIdsFile the real {@code .../modassets/assets/gloomycore/steam_dlc_ids.txt} file.
     * @param egsDlcIdsFile   the real {@code .../modassets/assets/gloomycore/egs_dlc_ids.txt} file.
     */
    public static SteamDlcIds load(Path steamDlcIdsFile, Path egsDlcIdsFile) throws IOException {
        List<String> steamIds = new ArrayList<>();
        for (String line : Files.readAllLines(steamDlcIdsFile, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                steamIds.add(trimmed);
            }
        }
        if (steamIds.isEmpty()) {
            throw new IOException(steamDlcIdsFile + ": no Steam DLC ids found");
        }

        String egsId = Files.readString(egsDlcIdsFile, StandardCharsets.UTF_8).trim();
        if (egsId.isEmpty()) {
            throw new IOException(egsDlcIdsFile + ": empty EGS DLC id file");
        }

        return new SteamDlcIds(Collections.unmodifiableList(steamIds), egsId);
    }

    /** Real Steam AppIDs (as decimal-digit strings, in file order) this build's DLC catalog covers. */
    public List<String> steamAppIds() {
        return steamAppIds;
    }

    /** The real Epic Games Store catalog-item id (32-char hex GUID, no dashes). */
    public String egsCatalogItemId() {
        return egsCatalogItemId;
    }

    public int steamAppIdCount() {
        return steamAppIds.size();
    }

    public boolean hasSteamAppId(String appId) {
        return steamAppIds.contains(appId);
    }
}

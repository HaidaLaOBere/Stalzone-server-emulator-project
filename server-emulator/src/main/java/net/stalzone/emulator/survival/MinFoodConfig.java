package net.stalzone.emulator.survival;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads {@code modassets/assets/stalker/min_food.txt} -- a real, plain-text file confirmed by
 * direct inspection (part 50) to contain exactly one thing: a single bare integer, no label, no
 * unit, no surrounding text (2 bytes on disk, {@code "10"}, no trailing newline or CR). It lives
 * in the exact same real client directory as {@code weight.txt} ({@link
 * net.stalzone.emulator.items.ItemWeightTable}), {@code detectors.txt} ({@link
 * net.stalzone.emulator.hazards.DetectorConfig}), and {@code moving.txt} ({@link
 * net.stalzone.emulator.movement.MovementConfig}) -- {@code modassets/assets/stalker/} --
 * confirming this project has now read every real plain-text config file in that directory.
 *
 * <p><b>What is and is not evidenced here, stated plainly per this project's rule 1:</b> the
 * FILE, its exact byte content, and its location are all directly confirmed real data -- nothing
 * about that is invented. What is NOT confirmed is what the number semantically means beyond what
 * its filename literally suggests: this project searched broadly for corroborating evidence (real
 * {@code config/keybindings} and {@code config/options.json} for any food/hunger-related key,
 * plus every other real file under {@code modassets/assets/} whose name matches {@code food},
 * {@code hunger}, {@code thirst}, or {@code water} -- part 50) and found nothing that names a
 * field, unit, or scale for this value; the only other food/hunger-adjacent real path found was
 * {@code modassets/assets/stalker/hungerBuffs/}, which turned out to contain a single UI icon
 * sprite sheet ({@code kit.mic}), not a data file. So: a real STALZONE survival mechanic
 * involving a minimum food/hunger threshold of {@code 10} is evidenced to exist by this file's
 * mere presence and content, but this project has NO evidence for what unit that {@code 10} is
 * in (a 0-100 hunger percentage? a raw food-item-value sum? something else?), what happens when a
 * player's food drops below it, or whether any decay/consumption rate exists anywhere in client
 * data -- none of that is invented here, and {@link #minFood()} is deliberately the only accessor
 * this class has.
 */
public final class MinFoodConfig {

    private final int minFood;

    private MinFoodConfig(int minFood) {
        this.minFood = minFood;
    }

    public static MinFoodConfig load(Path minFoodTxtPath) throws IOException {
        String text = Files.readString(minFoodTxtPath, StandardCharsets.UTF_8).trim();
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IOException(minFoodTxtPath + ": expected a single bare integer, got \"" + text + "\"", e);
        }
        return new MinFoodConfig(value);
    }

    /**
     * The single real integer this file contains -- confirmed {@code 10} in the client copy this
     * project has examined. See this class's doc for exactly what is and is not evidenced about
     * what this value means.
     */
    public int minFood() {
        return minFood;
    }

    @Override
    public String toString() {
        return "MinFoodConfig{minFood=" + minFood + "}";
    }
}

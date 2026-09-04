package net.stalzone.emulator.clientdata;

import net.stalzone.emulator.json.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loads the real {@code config/waypoints.cfg} file (a plain JSON array of {@link Waypoint}s) -- see that class's doc for the real schema and its transient/rewritten-per-session behavior. */
public final class WaypointList {

    private WaypointList() {
    }

    @SuppressWarnings("unchecked")
    public static List<Waypoint> load(Path path) throws IOException {
        String text = Files.readString(path, StandardCharsets.UTF_8);
        Object parsed = JsonReader.parse(text);
        if (!(parsed instanceof List)) {
            throw new IOException(path + ": expected a top-level JSON array, got "
                    + (parsed == null ? "null" : parsed.getClass().getSimpleName()));
        }
        List<Object> rawList = (List<Object>) parsed;
        List<Waypoint> out = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
            if (o instanceof java.util.Map) {
                out.add(Waypoint.fromRaw((java.util.Map<String, Object>) o));
            }
        }
        return Collections.unmodifiableList(out);
    }
}

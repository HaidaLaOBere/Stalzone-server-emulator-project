package net.stalzone.emulator.world.region;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Command-line sanity check for RegionFile, mirroring nbt.NbtDumpTool's usage style: reads
 * one .mdat file (or every .mdat in a directory, e.g. a map_cache/5.0/&lt;zone&gt;/ folder),
 * parses it, and prints a summary so a run of this can be visually spot-checked without
 * needing to trust the parser blindly. Usage:
 *   java -cp out net.stalzone.emulator.world.region.RegionDumpTool <path-to-.mdat-or-folder>
 *
 * This is the same summary shape (chunk counts, section counts, block-id histogram) that was
 * used to validate RegionFile against onejeuu/sc-file's own decoder byte-for-byte during
 * development -- see docs/RESEARCH_LOG.md part 15/16 for that validation writeup.
 */
public final class RegionDumpTool {

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        if (args.length < 1) {
            System.err.println("usage: RegionDumpTool <path-to-.mdat-file-or-folder>");
            System.exit(2);
        }

        Path input = Path.of(args[0]);
        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(input)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(input, "*.mdat")) {
                for (Path p : stream) {
                    files.add(p);
                }
            }
            files.sort(Path::compareTo);
            if (files.isEmpty()) {
                System.err.println("no .mdat files found in " + input);
                System.exit(1);
            }
        } else {
            files.add(input);
        }

        int totalChunks = 0;
        int totalSections = 0;
        Map<Integer, Long> globalHist = new TreeMap<>();

        for (Path path : files) {
            RegionFile region = RegionFile.read(path);
            int chunkCount = region.chunks().size();
            totalChunks += chunkCount;
            System.out.println(path.getFileName() + " -> region(" + region.regionX + "," + region.regionZ
                    + ")  chunks=" + chunkCount);

            int sectionsHere = 0;
            for (RegionChunk chunk : region.chunks()) {
                sectionsHere += chunk.sections.size();
                for (ChunkSection section : chunk.sections) {
                    for (int y = 0; y < ChunkSection.SIZE; y++) {
                        for (int z = 0; z < ChunkSection.SIZE; z++) {
                            for (int x = 0; x < ChunkSection.SIZE; x++) {
                                globalHist.merge(section.blockIdAt(x, y, z), 1L, Long::sum);
                            }
                        }
                    }
                }
            }
            totalSections += sectionsHere;
            System.out.println("  sections=" + sectionsHere);
        }

        System.out.println();
        System.out.println("TOTAL: " + files.size() + " region file(s), " + totalChunks + " chunk(s), "
                + totalSections + " section(s), " + globalHist.size() + " distinct block ids");
        System.out.println("Top block ids by voxel count:");
        globalHist.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> System.out.println("  id=" + e.getKey() + "  count=" + e.getValue()));
    }
}

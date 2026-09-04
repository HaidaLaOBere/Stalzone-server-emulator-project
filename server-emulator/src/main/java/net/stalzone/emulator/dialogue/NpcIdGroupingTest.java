package net.stalzone.emulator.dialogue;

import net.stalzone.emulator.testing.TestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.stalzone.emulator.testing.Assertions.*;
import static net.stalzone.emulator.testing.TestRunner.test;

/** Regression tests for the real-npcId-grouping heuristic (part 38) -- uses real npcId strings already cited in this project's docs, not synthetic ones. */
public final class NpcIdGroupingTest {

    public static List<TestRunner.NamedTest> tests() {
        List<TestRunner.NamedTest> out = new ArrayList<>();

        out.add(test("rawPrefixToken: real multi-underscore npcId takes the token before the FIRST underscore", () -> {
            assertEquals("duty", NpcIdGrouping.rawPrefixToken("duty_2_6yushka"), "duty_2_6yushka");
            assertEquals("zarya", NpcIdGrouping.rawPrefixToken("zarya_recruiter_1"), "zarya_recruiter_1");
        }));

        out.add(test("rawPrefixToken: real npcId with no underscore at all returns itself whole (skalolaz, part 34)", () -> {
            assertEquals("skalolaz", NpcIdGrouping.rawPrefixToken("skalolaz"), "skalolaz (no underscore)");
        }));

        out.add(test("rawPrefixToken: the real awkward one-letter token from p_fil_2/p_fil_3 (part 34)", () -> {
            assertEquals("p", NpcIdGrouping.rawPrefixToken("p_fil_2"), "p_fil_2");
        }));

        out.add(test("groupByRawPrefixToken: real spelling-variant split (terentii vs terentiy, part 34) stays UNMERGED", () -> {
            Map<String, List<String>> groups = NpcIdGrouping.groupByRawPrefixToken(
                    Arrays.asList("terentii_na_okraine", "terentii_verse", "terentiy_skuchno"));
            assertEquals(2, groups.size(), "distinct tokens (terentii, terentiy stay separate, not force-merged)");
            assertTrue(groups.containsKey("terentii"), "terentii token present");
            assertTrue(groups.containsKey("terentiy"), "terentiy token present");
            assertEquals(2, groups.get("terentii").size(), "terentii group size");
            assertEquals(1, groups.get("terentiy").size(), "terentiy group size");
        }));

        out.add(test("groupByRawPrefixToken: members within a group come back sorted", () -> {
            Map<String, List<String>> groups = NpcIdGrouping.groupByRawPrefixToken(
                    Arrays.asList("duty_3_8davidov", "duty_2_6yushka", "duty_1_aaa"));
            List<String> duty = groups.get("duty");
            assertEquals(Arrays.asList("duty_1_aaa", "duty_2_6yushka", "duty_3_8davidov"), duty, "duty group sorted order");
        }));

        return out;
    }
}

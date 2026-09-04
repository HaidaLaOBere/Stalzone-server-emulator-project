package net.stalzone.emulator.session.save;

import java.util.Map;

/**
 * One entry from a real per-character {@code common} save file's nested {@code
 * caseLastOpenCount.caseLastOpenCount} list (part 47) -- see {@link PlayerSaveFile} for where
 * this lives. Real schema, confirmed identical across two real characters, both real samples
 * having exactly one real entry so far: {@code {caseId: 15081, openAmount: &lt;int&gt;}} (real
 * {@code caseId} 15081 in both; real {@code openAmount} differed, 20 vs 1 -- confirming this is a
 * genuine per-character counter, not a fixed constant). Both fields are real NBT {@code TAG_INT}
 * values. Only one real {@code caseId} has been observed so far, so this project has no evidence
 * for what other case ids exist or what "case" 15081 actually is beyond its numeric id.
 */
public final class CaseOpenCount {

    private final Map<String, Object> fields;

    CaseOpenCount(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, Object> rawFields() {
        return fields;
    }

    public int caseId() {
        return intField("caseId");
    }

    public int openAmount() {
        return intField("openAmount");
    }

    private int intField(String key) {
        Object v = fields.get(key);
        return (v instanceof Integer) ? (Integer) v : 0;
    }

    @Override
    public String toString() {
        return "CaseOpenCount{caseId=" + caseId() + ", openAmount=" + openAmount() + "}";
    }
}

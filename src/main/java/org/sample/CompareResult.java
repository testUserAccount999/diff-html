package org.sample;

import java.util.List;

public class CompareResult {
    private boolean diff = false;
    private List<CompareDescription> compareDescriptions;
    private String oldText = "";
    private String newText = "";

    public CompareResult() {
    }

    public CompareResult(String o, String n, List<CompareDescription> compareDescriptions, boolean diff) {
        this.oldText = o.trim();
        this.newText = n.trim();
        this.compareDescriptions = compareDescriptions;
        this.diff = diff;
    }

    public boolean isDiff() {
        return diff;
    }

    @Deprecated
    public String getOldText() {
        return oldText;
    }

    @Deprecated
    public String getNewText() {
        return newText;
    }

    public List<CompareDescription> getCompareDescriptions() {
        return compareDescriptions;
    }

}
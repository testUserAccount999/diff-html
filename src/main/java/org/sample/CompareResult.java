package org.sample;

import java.util.List;

public class CompareResult {
    private boolean diff = false;
    private List<String> descriptions;
    private String oldText = "";
    private String newText = "";

    public CompareResult() {
    }

    public CompareResult(String o, String n, boolean diff, List<String> descriptions) {
        this.oldText = o.trim();
        this.newText = n.trim();
        this.diff = diff;
        this.descriptions = descriptions;
    }

    public boolean isDiff() {
        return diff;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public String getOldText() {
        return oldText;
    }

    public String getNewText() {
        return newText;
    }

}
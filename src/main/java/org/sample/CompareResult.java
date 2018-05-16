package org.sample;

public class CompareResult {
    private boolean diff = false;
    private String description = "";
    private String oldText = "";
    private String newText = "";

    public CompareResult() {
    }

    public CompareResult(String o, String n, boolean diff, String description) {
        this.oldText = o.trim();
        this.newText = n.trim();
        this.diff = diff;
        this.description = description;
    }

    public boolean isDiff() {
        return diff;
    }

    public String getDescription() {
        return description;
    }

    public String getOldText() {
        return oldText;
    }

    public String getNewText() {
        return newText;
    }

}
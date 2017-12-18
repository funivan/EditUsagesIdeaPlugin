package com.funivan.idea.editUsages.Structures;

/**
 * @author Ivan Shcherbak <dev@funivan.com>
 */
public class ReplaceStructure {
    private String value;
    private int line;


    public ReplaceStructure(String value, int line) {
        this.value = value;
        this.line = line;

    }

    public int getLine() {
        return line;
    }


    public String getValue() {
        return value;
    }
}

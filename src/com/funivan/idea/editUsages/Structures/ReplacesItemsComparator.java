package com.funivan.idea.editUsages.Structures;

import java.util.Comparator;

/**
 * @author Ivan Shcherbak <dev@funivan.com>
 */
public class ReplacesItemsComparator implements Comparator<ReplaceStructure> {

    @Override
    public int compare(ReplaceStructure o1, ReplaceStructure o2) {
        return (o1.getLine() < o2.getLine()) ? 1 : -1;
    }
}

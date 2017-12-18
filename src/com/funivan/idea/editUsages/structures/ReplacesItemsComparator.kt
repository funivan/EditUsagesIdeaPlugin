package com.funivan.idea.editUsages.structures

import java.util.Comparator

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class ReplacesItemsComparator : Comparator<ReplaceStructure> {
    override fun compare(o1: ReplaceStructure, o2: ReplaceStructure): Int {
        return if (o1.line < o2.line) 1 else -1
    }
}

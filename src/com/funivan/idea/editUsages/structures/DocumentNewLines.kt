package com.funivan.idea.editUsages.structures

import com.intellij.openapi.editor.Document
import java.util.*

/**
 * @author Ivan Shcherbak dev@funivan.com
 */
class DocumentNewLines(val document: Document) {
    val replaces = ArrayList<ReplaceStructure>()
}

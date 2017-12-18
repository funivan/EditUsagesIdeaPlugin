package com.funivan.idea.editUsages.structures

import com.intellij.openapi.editor.Document
import java.util.ArrayList

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class DocumentNewLines(val document: Document) {
    val replaces = ArrayList<ReplaceStructure>()
}

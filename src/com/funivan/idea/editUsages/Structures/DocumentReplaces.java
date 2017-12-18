package com.funivan.idea.editUsages.Structures;

import com.intellij.openapi.editor.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan Shcherbak <dev@funivan.com>
 */
public class DocumentReplaces {

    private Document document;
    private List<ReplaceStructure> replaces = new ArrayList<>();

    public DocumentReplaces(Document document) {
        this.document = document;
    }

    public void add(ReplaceStructure replaceStructure) {
        replaces.add(replaceStructure);
    }

    public List<ReplaceStructure> getReplaces() {
        return replaces;
    }

    public Document getDocument() {
        return document;
    }
}

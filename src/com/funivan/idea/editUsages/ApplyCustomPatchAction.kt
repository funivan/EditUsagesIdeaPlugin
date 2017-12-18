package com.funivan.idea.editUsages

import com.funivan.idea.editUsages.structures.DocumentNewLines
import com.funivan.idea.editUsages.structures.ReplaceStructure
import com.funivan.idea.editUsages.structures.ReplacesItemsComparator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.awt.RelativePoint
import java.util.*
import java.util.regex.Pattern

@Suppress("NAME_SHADOWING")
/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class ApplyCustomPatchAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val dataContext = event.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return

        val document = editor.document
        val text = document.text

        val lines = text.split("//file:".toRegex()).dropLastWhile({ it.isEmpty() }).iterator()
        val pattern = Pattern.compile("^([^\n]+):(\\d+)\n(.+)$", Pattern.DOTALL)
        val documentsForChange = HashMap<String, DocumentNewLines>()
        for (line in lines) {
            val match = pattern.matcher(line)
            if (!match.find()) {
                continue
            }
            val filePath = match.group(1)
            val file = VfsUtil.findRelativeFile(filePath, project.baseDir) ?: continue
            val lineNumber = Integer.parseInt(match.group(2)) - 1
            var value = match.group(3)
            value = value.replace("\n+$".toRegex(), "")
            val fileDocument = FileDocumentManager.getInstance().getDocument(file)
            if (fileDocument == null) {
                continue
            }
            var documentNewStructure: DocumentNewLines? = documentsForChange[filePath]
            if (documentNewStructure == null) {
                documentNewStructure = DocumentNewLines(fileDocument)
                documentsForChange.put(filePath, documentNewStructure)
            }
            documentNewStructure.replaces.add(ReplaceStructure(value, lineNumber))
        }

        CommandProcessor.getInstance().executeCommand(project, {
            ApplicationManager.getApplication().runWriteAction {
                var items = 0
                var files = 0
                for (doc in documentsForChange.values) {

                    val replaces = doc.replaces

                    // We need to change elements from the bottom to the top of the document.
                    // So sort them by start position
                    Collections.sort(replaces, ReplacesItemsComparator())

                    val document = doc.document
                    for (r in replaces) {
                        val startOffset = document.getLineStartOffset(r.line)
                        val endOffset = document.getLineEndOffset(r.line)
                        document.replaceString(startOffset, endOffset, r.value)
                        items++
                    }
                    files++
                }

                val statusBar = WindowManager.getInstance().getStatusBar(project)
                if (statusBar != null) {
                    JBPopupFactory.getInstance()
                            .createHtmlTextBalloonBuilder("Processed. Files: $files lines:$items", MessageType.INFO, null)
                            .setFadeoutTime(7500)
                            .createBalloon()
                            .show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.atRight)

                }
            }
        }, "Apply custom patch", "Apply custom patch")

    }
}

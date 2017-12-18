package com.funivan.idea.editUsages

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsageView
import com.intellij.usages.impl.UsageViewImpl
import java.util.*

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class CreateCustomPatchAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val dataContext = e.dataContext
            val usageView = UsageView.USAGE_VIEW_KEY.getData(dataContext) as? UsageViewImpl ?: return
            var usages = usageView.selectedUsages
            if (usages.size == 0) {
                usages = usageView.usages
            }
            val baseDir = project.baseDir
            val buffer = StringBuilder()
            val processedLines = HashMap<String, Boolean>()
            CommandProcessor.getInstance().executeCommand(project, {
                ApplicationManager.getApplication().runWriteAction {
                    var language: Language? = null
                    for (usage in usages) {
                        if (usage !is UsageInfo2UsageAdapter || usage.element == null) {
                            continue
                        }
                        val file = usage.file
                        val line = usage.line
                        val key = file.path + ":" + line
                        if (processedLines[key] != null) {
                            continue
                        }
                        language = usage.element.language
                        val fileDocument = FileDocumentManager.getInstance().getDocument(file) ?: continue
                        val startOffset = fileDocument.getLineStartOffset(line)
                        val endOffset = fileDocument.getLineEndOffset(line)
                        val text = fileDocument.getText(TextRange(startOffset, endOffset))
                        buffer.append("\n")
                        buffer.append("//file:")
                                .append(VfsUtil.getRelativePath(file, baseDir, '/'))
                                .append(':').append(line + 1)
                                .append("\n")
                        buffer.append(text).append("\n")
                        buffer.append("\n")
                        processedLines.put(key, true)
                    }
                    val scratchFile = ScratchRootType.getInstance()
                            .createScratchFile(
                                    project,
                                    "scratch",
                                    language,
                                    buffer.toString(),
                                    ScratchFileService.Option.create_new_always
                            )
                    if (scratchFile != null) {
                        FileEditorManager.getInstance(project).openFile(scratchFile, true)
                    }
                }
            }, "Create custom patch", "Create custom patch")

        }
    }


}
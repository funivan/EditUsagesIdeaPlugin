package com.funivan.idea.editUsages;

import com.funivan.idea.editUsages.Structures.DocumentReplaces;
import com.funivan.idea.editUsages.Structures.ReplaceStructure;
import com.funivan.idea.editUsages.Structures.ReplacesItemsComparator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbModePermission;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.awt.RelativePoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author Ivan Shcherbak <dev@funivan.com>
 */
public class ApplyCustomPatch extends AnAction {

    public void actionPerformed(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }
        PsiDocumentManager.getInstance(project).commitAllDocuments();
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if (editor == null) {
            return;
        }
        Document document = editor.getDocument();


        String text = document.getText();

        String[] lines = text.split("//file:");

        Pattern pattern = Pattern.compile("^([^\n]+):(\\d+)\n(.+)$", Pattern.DOTALL);

        Map<String, DocumentReplaces> documentsForChange = new HashMap<>();

        for (String line : lines) {

            Matcher match = pattern.matcher(line);
            if (!match.find()) {
                System.out.println("No match:" + line);
                continue;
            }


            String filePath = match.group(1);

            VirtualFile file = VfsUtil.findRelativeFile(filePath, project.getBaseDir());

            if (file == null) {
                continue;
            }

            int lineNumber = (Integer.parseInt(match.group(2)) - 1);


            String value = match.group(3);

            value = value.replaceAll("\n+$", "");
            System.out.println(value);

            Document fileDocument = FileDocumentManager.getInstance().getDocument(file);

            if (fileDocument == null) {
                continue;
            }


            DocumentReplaces replaceStructure = documentsForChange.get(filePath);
            if (replaceStructure == null) {
                replaceStructure = new DocumentReplaces(fileDocument);
                documentsForChange.put(filePath, replaceStructure);
            }

            replaceStructure.add(new ReplaceStructure(value, lineNumber));

        }

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                DumbService.allowStartingDumbModeInside(DumbModePermission.MAY_START_BACKGROUND, new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                int items = 0;
                                int files = 0;
                                for (DocumentReplaces doc : documentsForChange.values()) {

                                    List<ReplaceStructure> replaces = doc.getReplaces();

                                    // We need to change elements from the bottom to the top of the document.
                                    // So sort them by start position
                                    Collections.sort(replaces, new ReplacesItemsComparator());

                                    Document document = doc.getDocument();

                                    for (ReplaceStructure r : replaces) {
                                        int startOffset = document.getLineStartOffset(r.getLine());
                                        int endOffset = document.getLineEndOffset(r.getLine());

                                        document.replaceString(startOffset, endOffset, r.getValue());
                                        items++;
                                    }
                                    files++;
                                }

                                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                                if (statusBar != null) {
                                    JBPopupFactory.getInstance()
                                            .createHtmlTextBalloonBuilder("Processed. Files: " + files + " lines:" + items, MessageType.INFO, null)
                                            .setFadeoutTime(7500)
                                            .createBalloon()
                                            .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);

                                }
                            }
                        });
                    }
                });
            }
        }, "Apply custom patch", "Apply custom patch");

    }
}

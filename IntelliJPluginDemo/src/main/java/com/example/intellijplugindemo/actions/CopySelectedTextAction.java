package com.example.intellijplugindemo.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class CopySelectedTextAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Get all the required data from data keys
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        Project project = event.getRequiredData(CommonDataKeys.PROJECT);
        Document document = editor.getDocument();

        // Work off of the primary caret to get the selection info
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();

        String searchText = primaryCaret.getSelectedText();
        boolean openBrowserSuccess =
                queryGoogle(translateStringToGoogleQuery(searchText));

        // De-select the text range that was just replaced
        if (openBrowserSuccess)
            primaryCaret.removeSelection();
    }

    public static boolean queryGoogle(String searchQuery){
        try {
            return openWebpage(new URL("https://www.google.com/search?q="+searchQuery));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String translateStringToGoogleQuery(String searchText){

        try {
            return URLEncoder.encode(
                    searchText.trim(),
                    StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Get required data keys
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);

        // Set visibility only in the case of
        // existing project editor, and selection
        event.getPresentation().setEnabledAndVisible(project != null
                && editor != null && editor.getSelectionModel().hasSelection());
    }
}

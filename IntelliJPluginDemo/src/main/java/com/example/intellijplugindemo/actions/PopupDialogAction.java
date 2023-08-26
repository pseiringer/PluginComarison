package com.example.intellijplugindemo.actions;

import com.example.intellijplugindemo.dialogs.SimpleInputForm;
import com.example.intellijplugindemo.dialogs.SimpleLoginDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;

public class PopupDialogAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {


//        // Using the event, create and show a dialog
//        Project currentProject = event.getProject();
//        StringBuilder message =
//                new StringBuilder(event.getPresentation().getText() + " Selected!");
//        // If an element is selected in the editor, add info about it.
//        Navigatable selectedElement = event.getData(CommonDataKeys.NAVIGATABLE);
//        if (selectedElement != null) {
//            message.append("\nSelected Element: ").append(selectedElement);
//        }
//        String title = event.getPresentation().getDescription();
////        Messages.showMessageDialog(
////                currentProject,
////                message.toString(),
////                title,
////                Messages.getInformationIcon());
//
//        if (new SimpleLoginDialog().showAndGet()){
//            Messages.showMessageDialog(
//                currentProject,
//                "ok pressed",
//                "dialog closed",
//                Messages.getInformationIcon());
//        }
//        else {
//            Messages.showMessageDialog(
//                    currentProject,
//                    "canceled",
//                    "dialog closed",
//                    Messages.getInformationIcon());
//        }

        var currentProject = event.getProject();

        final SimpleInputForm form = new SimpleInputForm(); //"GUI form" made in IntelliJ GUI designer

        DialogBuilder builder = new DialogBuilder();
        builder.setCenterPanel(form.getRoot());
//        builder.setDimensionServiceKey("MyInputDialog1");
        builder.setTitle("My Simple Input Form");
        builder.removeAllActions();
        builder.addOkAction();
        builder.addCancelAction();

        boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
        if (isOk) {
            Messages.showMessageDialog(
                currentProject,
                "ok pressed\n" +
                        "first = " + form.getFirst() +
                        "last = " + form.getLast() +
                        "pass = " + form.getPass() ,
                "dialog closed",
                Messages.getInformationIcon());
        }
        else {
            Messages.showMessageDialog(
                    currentProject,
                    "canceled",
                    "dialog closed",
                    Messages.getInformationIcon());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Set the availability based on whether a project is open
        Project currentProject = event.getProject();
        event.getPresentation().setEnabledAndVisible(currentProject != null);
    }
}

package com.example.intellijplugindemo.test;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.DoNotAskOption;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UITestAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        var result = new SimpleDialogWrapper(e.getProject()).showAndGet();
//        System.out.println("Dialog result:" + result);

//        JPanel dialogPanel = new JPanel(new BorderLayout());
//
//        JLabel label = new JLabel("testing");
//        label.setPreferredSize(new Dimension(100, 100));
//        dialogPanel.add(label, BorderLayout.CENTER);
//
//        JBPopupFactory.getInstance()
////                .createComponentPopupBuilder(
////                        dialogPanel,
////                        label
////                )
////                .createPopupChooserBuilder(Arrays.asList("Option 1", "Option 2", "Option 3"))
////                .createPopup()
//                .createConfirmation("Do you really want this?",
//                        "sure",
//                        "nope",
//                        () -> System.out.println("yes"),
//                        () -> System.out.println("no"),
//                        0)
//                .showInFocusCenter();
////                .showInBestPositionFor(e.getDataContext());


//        var editor = e.getRequiredData(CommonDataKeys.EDITOR);
//        HintManager.getInstance().showErrorHint(editor, "Hello Error");


        var proj = e.getProject();
        NotificationGroupManager.getInstance()
                .getNotificationGroup("CustomNotificationGroup")
                .createNotification("Error Message", NotificationType.ERROR)
                .notify(proj);
    }

    private class SimpleDialogWrapper extends DialogWrapper {
        public SimpleDialogWrapper(Project p){
            super(p);
            setTitle("My Simple Dialog");
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel dialogPanel = new JPanel(new BorderLayout());

            JLabel label = new JLabel("testing");
            label.setPreferredSize(new Dimension(100, 100));
            dialogPanel.add(label, BorderLayout.CENTER);

            return dialogPanel;
        }
    }

}

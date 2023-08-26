package com.example.intellijplugindemo.dialogs;

import com.intellij.openapi.ui.DialogPanel;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SimpleLoginDialog extends DialogWrapper {
    public SimpleLoginDialog() {
        super(true);
        setTitle("Test Simple Login Dialog");
        init();
    }
    @Override
    protected @Nullable JComponent createCenterPanel() {
//        JPanel dialogPanel = new JPanel(new BorderLayout());
//
//        JLabel label = new JLabel("testing");
//        label.setPreferredSize(new Dimension(100, 100));
//        dialogPanel.add(label, BorderLayout.CENTER);
//
//        return dialogPanel;

        JPanel dialogPanel = new JPanel();

        JLabel firstNameLbl = new JLabel("first name:");
        JTextField firstName = new JTextField();
        JLabel lastNameLbl = new JLabel("last name:");
        JTextField lastName = new JTextField();
        JLabel passwordLbl = new JLabel("password:");
        JPasswordField password = new JPasswordField();

        GroupLayout layout = new GroupLayout(dialogPanel);
        dialogPanel.setLayout(layout);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(firstNameLbl)
                                        .addComponent(firstName)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(lastNameLbl)
                                        .addComponent(lastName)
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(passwordLbl)
                                        .addComponent(password)
                        )
        );

        return  dialogPanel;
    }
}

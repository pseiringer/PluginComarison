package com.example.intellijplugindemo.dialogs;

import javax.swing.*;

public class SimpleInputForm {
    private JTextField inputLast;
    private JTextField inputFirst;
    private JPasswordField inputPass;
    private JPanel rootPanel;

    public JPanel getRoot(){
        return rootPanel;
    }

    public String getFirst(){
        return inputFirst.getText();
    }
    public String getLast(){
        return inputLast.getText();
    }
    public String getPass(){
        return inputPass.getText();
    }
}

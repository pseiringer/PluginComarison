package com.example.intellijplugindemo.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import java.util.function.Predicate;

public class RecentChangesSettingsComponent {

    private final JPanel myMainPanel;
    private final JBTextField debounceTimeText = new JBTextField();
    private final JBTextField queueSizeText = new JBTextField();

    public RecentChangesSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Debounce Time (ms): "), debounceTimeText, 1, false)
                .addLabeledComponent(new JBLabel("Queue Size: "), queueSizeText, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        // set filters
        var longFilter = new TestFilter(text -> {
            try {
                return Long.parseLong(text) > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        var intFilter = new TestFilter(text -> {
            try {
                return Integer.parseInt(text) > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        ((PlainDocument) debounceTimeText.getDocument()).setDocumentFilter(longFilter);
        ((PlainDocument) queueSizeText.getDocument()).setDocumentFilter(intFilter);
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return debounceTimeText;
    }

    @NotNull
    public long getDebounceTimeText() {
        return Long.parseLong(debounceTimeText.getText());
    }

    public void setDebounceTimeText(@NotNull long newVal) {
        debounceTimeText.setText(String.valueOf(newVal));
    }

    @NotNull
    public int getQueueSizeText() {
        return Integer.parseInt(queueSizeText.getText());
    }

    public void setQueueSizeText(@NotNull int newVal) {
        queueSizeText.setText(String.valueOf(newVal));
    }

    class TestFilter extends DocumentFilter {

        private Predicate<String> isValid;

        public TestFilter(Predicate<String> isValid){
            this.isValid = isValid;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.insert(offset, string);

            if (test(sb.toString())) {
                super.insertString(fb, offset, string, attr);
            } else {
                // warn the user and don't allow the insert
            }
        }

        private boolean test(String text) {
            return isValid.test(text);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.replace(offset, offset + length, text);

            if (test(sb.toString())) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                // warn the user and don't allow the insert
            }

        }

        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.delete(offset, offset + length);

            if (test(sb.toString())) {
                super.remove(fb, offset, length);
            } else {
                // warn the user and don't allow the insert
            }

        }
    }
}
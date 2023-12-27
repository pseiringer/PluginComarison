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
        // create a form with input fields for debounce time and queue size
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Debounce Time (ms): "), debounceTimeText, 1, false)
                .addLabeledComponent(new JBLabel("Queue Size: "), queueSizeText, 1, false)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        // set filters on input fields to prevent wrong user inputs
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

    /**
     * @return The JPanel with the settings fields.
     */
    public JPanel getPanel() {
        return myMainPanel;
    }

    /**
     * @return The JComponent that should be focused when the settings page is opened.
     */
    public JComponent getPreferredFocusedComponent() {
        return debounceTimeText;
    }

    /**
     * @return The currently selected debounce time.
     */
    @NotNull
    public long getDebounceTimeText() {
        return Long.parseLong(debounceTimeText.getText());
    }

    /**
     * @param newVal The new value for the currently selected debounce time.
     */
    public void setDebounceTimeText(@NotNull long newVal) {
        debounceTimeText.setText(String.valueOf(newVal));
    }

    /**
     * @return The currently selected queue size.
     */
    @NotNull
    public int getQueueSizeText() {
        return Integer.parseInt(queueSizeText.getText());
    }

    /**
     * @param newVal The new value for the currently selected queue size.
     */
    public void setQueueSizeText(@NotNull int newVal) {
        queueSizeText.setText(String.valueOf(newVal));
    }

    class TestFilter extends DocumentFilter {

        private Predicate<String> isValid;

        /**
         * @param isValid The Predicate to be applied to inputs to see if they are valid.
         */
        public TestFilter(Predicate<String> isValid){
            this.isValid = isValid;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {
            // get current string
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.insert(offset, string);

            // check if input is valid
            if (test(sb.toString())) {
                // valid -> insert the string
                super.insertString(fb, offset, string, attr);
            } else {
                // warn the user and don't allow the insert
            }
        }

        /**
         * Tests if the Predicate {@link #isValid} returns true for the given text.
         * @param text The text passed to {@link #isValid}.
         * @return The result of the call to {@link #isValid}.
         */
        private boolean test(String text) {
            return isValid.test(text);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {
            // get current string
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.replace(offset, offset + length, text);

            // check if input is valid
            if (test(sb.toString())) {
                // valid -> insert the string
                super.replace(fb, offset, length, text, attrs);
            } else {
                // warn the user and don't allow the insert
            }

        }

        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            // get current string
            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.delete(offset, offset + length);

            // check if input is valid
            if (test(sb.toString())) {
                // valid -> insert the string
                super.remove(fb, offset, length);
            } else {
                // warn the user and don't allow the insert
            }
        }
    }
}
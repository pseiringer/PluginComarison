package com.example.intellijplugindemo.toolwindows;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Objects;

public class RecentChangesToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        RecentChangesToolWindowContent toolWindowContent = new RecentChangesToolWindowContent(toolWindow);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class RecentChangesToolWindowContent {

        private static class RecentChangesListEntry{}
        private final JPanel contentPanel = new JPanel();
        private final Tree changesTree = new Tree();
        //private final changesservice ...
//        private final JLabel currentDate = new JLabel();

        public RecentChangesToolWindowContent(ToolWindow toolWindow) {
            contentPanel.setLayout(new BorderLayout(0, 20));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
//            contentPanel.add(createCalendarPanel(), BorderLayout.PAGE_START);
            contentPanel.add(createControlsPanel(toolWindow), BorderLayout.CENTER);
//            updateCurrentDateTime();
        }

//        @NotNull
//        private JPanel createCalendarPanel() {
//            JPanel calendarPanel = new JPanel();
//            calendarPanel.add(currentDate);
//            return calendarPanel;
//        }


        @NotNull
        private JPanel createControlsPanel(ToolWindow toolWindow) {
            JPanel controlsPanel = new JPanel();
            JButton refreshDateAndTimeButton = new JButton("Refresh");
//            refreshDateAndTimeButton.addActionListener(e -> updateCurrentDateTime());
            controlsPanel.add(refreshDateAndTimeButton);
            return controlsPanel;
        }

//        private void updateCurrentDateTime() {
//            currentDate.setText(getCurrentDate(calendar));
//        }

//        private String getCurrentDate(Calendar calendar) {
//            return calendar.get(Calendar.DAY_OF_MONTH) + "/"
//                    + (calendar.get(Calendar.MONTH) + 1) + "/"
//                    + calendar.get(Calendar.YEAR);
//        }

        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}

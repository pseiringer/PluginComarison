package com.example.intellijplugindemo.toolwindows;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

public class RecentChangesToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        RecentChangesToolWindowContent toolWindowContent = new RecentChangesToolWindowContent(toolWindow);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class RecentChangesToolWindowContent {
        private final JPanel contentPanel = new JPanel();
        private DefaultTreeModel model;
        private Tree changesTree = new Tree();

        public RecentChangesToolWindowContent(ToolWindow toolWindow) {
            // set up the overall layout of the toolwindow
            contentPanel.setLayout(new BorderLayout(0, 20));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            contentPanel.add(createChangesTreePanel(), BorderLayout.CENTER);
            // update the content
            updateRecentChangesTree();
            // subscribe to changes service
            RecentChangesService.getInstance().addChangeListener(() -> {
                updateRecentChangesTree();
            });
        }

        @NotNull
        private JPanel createChangesTreePanel() {
            // create the root node and model of the tree
            var root = new DefaultMutableTreeNode("Root Node");
            model = new DefaultTreeModel(root);

            changesTree = new Tree(model);

            // create the panel to display the tree
            JPanel treePanel = new JPanel();
            treePanel.add(changesTree);
            return treePanel;
        }
        
        private void updateRecentChangesTree() {
            var root = (DefaultMutableTreeNode) model.getRoot();

            // clear the tree
            root.removeAllChildren();
            model.reload(root);

            // add children for all recent changes
            RecentChangesService.getInstance()
                    .getRecentChanges().
                    descendingIterator().forEachRemaining(diff -> {
                // add diff node
                DefaultMutableTreeNode diffNode = new DefaultMutableTreeNode(
                        String.format("'%s' -> '%s'", diff.getRemovedText(), diff.getReplacementText()),
                        true);
                model.insertNodeInto(diffNode, root, root.getChildCount());

                // add children of diff node
                DefaultMutableTreeNode removalNode = new DefaultMutableTreeNode(
                        String.format("Removes '%s'", diff.getRemovedText()),
                        false);
                model.insertNodeInto(removalNode, diffNode, diffNode.getChildCount());
                DefaultMutableTreeNode replacementNode = new DefaultMutableTreeNode(
                        String.format("Adds '%s'", diff.getReplacementText()),
                        false);
                model.insertNodeInto(replacementNode, diffNode, diffNode.getChildCount());
            });

            // hide the root so only the changes are visible
            changesTree.setRootVisible(false);
            changesTree.setShowsRootHandles(true);
            changesTree.expandPath(new TreePath(root.getPath()));
        }


        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}

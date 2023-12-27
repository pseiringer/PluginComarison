package com.example.intellijplugindemo;


import com.example.intellijplugindemo.eventlisteners.SimpleChangeDocumentListener;
import com.example.intellijplugindemo.services.IsRecentChangesRunningService;
import com.example.intellijplugindemo.services.RecentChangesService;
import com.example.intellijplugindemo.services.RecentChangesSettingsService;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class Startup implements StartupActivity {


    @Override
    public void runActivity(@NotNull Project project) {
        startRecentChangesPlugin(project);
    }

    /**
     * Initializes all services and listeners needed for the plugin.
     * @param project
     */
    public static synchronized void startRecentChangesPlugin(Project project){
        // check if the plugin is already running
        var isRunningService = IsRecentChangesRunningService.getInstance();
        if (isRunningService.isRunning())
            return;

        System.out.println("===> Started Service");

        // register document listener for all documents
        var appLevelService = RecentChangesService.getInstance();
        var eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.addDocumentListener(new SimpleChangeDocumentListener(), appLevelService);

        // set the plugin to running
        isRunningService.setRunning();
    }
}
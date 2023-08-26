package com.example.intellijplugindemo;


import com.example.intellijplugindemo.eventlisteners.SimpleChangeDocumentListener;
import com.example.intellijplugindemo.services.RecentChangesService;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class Startup implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        var projLevelService = RecentChangesService.getInstance();
        var eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.addDocumentListener(new SimpleChangeDocumentListener(project), projLevelService);
    }
}
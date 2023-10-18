package com.example.intellijplugindemo.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;

@Service(Service.Level.APP)
public final class IsRecentChangesRunningService implements Disposable {

    private boolean running = false;

    public static IsRecentChangesRunningService getInstance(){
        return ApplicationManager.getApplication().getService(IsRecentChangesRunningService.class);
    }

    public void setRunning() {
        this.running = true;
    }
    public boolean isRunning() {
        return running;
    }

    @Override
    public void dispose() {

    }
}

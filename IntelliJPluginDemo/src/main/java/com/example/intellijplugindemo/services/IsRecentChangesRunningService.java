package com.example.intellijplugindemo.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;

@Service(Service.Level.APP)
public final class IsRecentChangesRunningService implements Disposable {

    private boolean running = false;

    /**
     * @return The current Instance of {@link IsRecentChangesRunningService}.
     */
    public static IsRecentChangesRunningService getInstance(){
        return ApplicationManager.getApplication().getService(IsRecentChangesRunningService.class);
    }

    /**
     * Sets {@link #running} to true.
     */
    public void setRunning() {
        this.running = true;
    }

    /**
     * @return The current state of {@link #running}.
     */
    public boolean isRunning() {
        return running;
    }

    @Override
    public void dispose() {
        // nothing to do
    }
}

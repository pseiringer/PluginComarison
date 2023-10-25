package com.example.intellijplugindemo.settings;

import com.example.intellijplugindemo.services.RecentChangesSettingsService;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RecentChangesSettingsConfigurable implements Configurable {
    //TODO apply settings to code
    private RecentChangesSettingsComponent mySettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered in an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Recent Changes Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new RecentChangesSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        RecentChangesSettingsService settings = RecentChangesSettingsService.getInstance();
        boolean modified = mySettingsComponent.getDebounceTimeText() != settings.getDebounceTimeMs();
        modified |= mySettingsComponent.getQueueSizeText() != settings.getQueueSize();
        return modified;
    }

    @Override
    public void apply() {
        RecentChangesSettingsService settings = RecentChangesSettingsService.getInstance();
        settings.setDebounceTimeMs(mySettingsComponent.getDebounceTimeText());
        settings.setQueueSize(mySettingsComponent.getQueueSizeText());
    }

    @Override
    public void reset() {
        RecentChangesSettingsService settings = RecentChangesSettingsService.getInstance();
        mySettingsComponent.setDebounceTimeText(settings.getDebounceTimeMs());
        mySettingsComponent.setQueueSizeText(settings.getQueueSize());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}

package com.example.intellijplugindemo.settings;

import com.example.intellijplugindemo.services.RecentChangesSettingsService;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RecentChangesSettingsConfigurable implements Configurable {
    private RecentChangesSettingsComponent mySettingsComponent;

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
        // initialize the settings component and return its panel
        mySettingsComponent = new RecentChangesSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        // get the service containing the persisted settings
        RecentChangesSettingsService settingsService = RecentChangesSettingsService.getInstance();
        // check if the current settings deviate from the settings stored in the service
        boolean modified = mySettingsComponent.getDebounceTimeText() != settingsService.getDebounceTimeMs();
        modified |= mySettingsComponent.getQueueSizeText() != settingsService.getQueueSize();
        return modified;
    }

    @Override
    public void apply() {
        // persist the current settings in the service
        RecentChangesSettingsService settings = RecentChangesSettingsService.getInstance();
        settings.setDebounceTimeMs(mySettingsComponent.getDebounceTimeText());
        settings.setQueueSize(mySettingsComponent.getQueueSizeText());
    }

    @Override
    public void reset() {
        // reset the displayed fields to show the persisted settings from the service
        RecentChangesSettingsService settings = RecentChangesSettingsService.getInstance();
        mySettingsComponent.setDebounceTimeText(settings.getDebounceTimeMs());
        mySettingsComponent.setQueueSizeText(settings.getQueueSize());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}

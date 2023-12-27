package com.example.intellijplugindemo.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

@State(
        name = "org.intellij.sdk.settings.AppSettingsState",
        storages = @Storage("RecentChangesSettings.xml")
)
public class RecentChangesSettingsService implements PersistentStateComponent<RecentChangesSettingsService.RecentChangesSettingsState> {

    public static class RecentChangesSettingsState {
        public long debounceTimeMs = 750L;
        public int queueSize = 10;
    }

    private RecentChangesSettingsState state = new RecentChangesSettingsState();
    private ArrayList<Consumer<Integer>> queueSizeListeners = new ArrayList<>();

    /**
     * Returns the settings state that should be persisted.
     * @inheritDoc
     */
    @Override
    public @Nullable RecentChangesSettingsState getState() {
        return state;
    }

    /**
     * Loads the read state into the current state of settings.
     * @inheritDoc
     */
    @Override
    public void loadState(@NotNull RecentChangesSettingsState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    /**
     * @return The current Instance of {@link RecentChangesSettingsService}.
     */
    public static RecentChangesSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(RecentChangesSettingsService.class);
    }


    /**
     * @return The currently selected debounce time.
     */
    public long getDebounceTimeMs() {
        return state.debounceTimeMs;
    }
    /**
     * @param debounceTimeMs The new value for the currently selected debounce time.
     */
    public void setDebounceTimeMs(long debounceTimeMs) {
        state.debounceTimeMs = debounceTimeMs;
    }

    /**
     * @return The currently selected queue size.
     */
    public int getQueueSize() {
        return state.queueSize;
    }
    /**
     * @param queueSize The new value for the currently selected queue size.
     */
    public void setQueueSize(int queueSize) {
        state.queueSize = queueSize;
        queueSizeListeners.forEach(c -> c.accept(state.queueSize));
    }

    /**
     * Adds a listener to the list of subscribed listeners.
     * @param consumer The listener to be added.
     */
    public void addQueueSizeListener(Consumer<Integer> consumer){
        queueSizeListeners.add(consumer);
    }
    /**
     * Removes a listener from the list of subscribed listeners.
     * @param consumer The listener to be removed.
     */
    public void removeQueueSizeListener(Consumer<Integer> consumer){
        queueSizeListeners.remove(consumer);
    }
}

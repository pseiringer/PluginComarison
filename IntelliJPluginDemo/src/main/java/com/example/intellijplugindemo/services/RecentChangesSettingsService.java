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

    @Override
    public @Nullable RecentChangesSettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull RecentChangesSettingsState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    public static RecentChangesSettingsService getInstance() {
        return ApplicationManager.getApplication().getService(RecentChangesSettingsService.class);
    }


    public long getDebounceTimeMs() {
        return state.debounceTimeMs;
    }

    public void setDebounceTimeMs(long debounceTimeMs) {
        state.debounceTimeMs = debounceTimeMs;
    }

    public int getQueueSize() {
        return state.queueSize;
    }

    public void setQueueSize(int queueSize) {
        state.queueSize = queueSize;
        queueSizeListeners.forEach(c -> c.accept(state.queueSize));
    }

    public void addQueueSizeListener(Consumer<Integer> consumer){
        queueSizeListeners.add(consumer);
    }
    public void removeQueueSizeListener(Consumer<Integer> consumer){
        queueSizeListeners.remove(consumer);
    }
}

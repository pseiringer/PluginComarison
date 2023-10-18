package com.example.test;

import com.example.intellijplugindemo.services.RecentChangesService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.testFramework.UsefulTestCase.*;

public class RecentChangesServiceTest {

    @Test
    void addRecentChangeDispatchesStorageChangedEvent() {
        // Arrange.
        var changeStorage = RecentChangesService.getInstance();

        AtomicBoolean eventCalled = new AtomicBoolean(false);
        changeStorage.addChangeListener(() -> {
            eventCalled.set(true);
        });

        var diff = new RecentChangesService.SimpleDiff();

        // Act.
        changeStorage.addChange(diff);

        // Assert.
        assertTrue("The event callback has not been called", eventCalled.get());
    }

    @Test
    void addRecentChangeAddsSimpleDiff() {
        // Arrange.
        final int numDiffs = 5;

        var changeStorage = RecentChangesService.getInstance();

        var diffs = new ArrayList<RecentChangesService.SimpleDiff>();
        for (int i = 0; i < numDiffs; i++){
            var diff = new RecentChangesService.SimpleDiff();
            diff.setRemovedText("diff"+i);
            diffs.add(diff);
        }

        // Act.
        for (var diff : diffs) {
            changeStorage.addChange(diff);
        }

        // Assert.
        assertSameElements(changeStorage.getRecentChanges(), diffs);
    }

}

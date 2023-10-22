package com.example.test;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


public class RecentChangesServiceTest extends BasePlatformTestCase {

    @BeforeEach
    @Override
    protected void setUp() throws Exception{
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception{
        RecentChangesService.getInstance().reset();
        super.tearDown();
    }

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

    @Test
    void addRecentChangeEvictsOldSimpleDiff() {
        // Arrange.
        final int evictedDiffs = 2;
        final int numDiffs = RecentChangesService.QUEUE_SIZE + evictedDiffs;

        var changeStorage = RecentChangesService.getInstance();

        var diffs = new ArrayList<RecentChangesService.SimpleDiff>();
        var removedDiffs = new ArrayList<RecentChangesService.SimpleDiff>();
        var containedDiffs = new ArrayList<RecentChangesService.SimpleDiff>();
        for (int i = 0; i < numDiffs; i++) {
            var diff = new RecentChangesService.SimpleDiff();
            diff.setRemovedText("diff"+i);
            diffs.add(diff);
            if (i < evictedDiffs) {
                removedDiffs.add(diff);
            }
            else {
                containedDiffs.add(diff);
            }
        }

        // Act.
        for (var i = 0; i < numDiffs; i++) {
            changeStorage.addChange(diffs.get(i));
        }

        // Assert.
        var result = changeStorage.getRecentChanges();
        assertSameElements(result, containedDiffs);
        removedDiffs.forEach(diff -> {
            assertDoesntContain(result, diff);
        });
    }

    @Test
    void getDiffMatchingRemovedTextReturnsFittingSimpleDiffOnMatch() {
        // Arrange.
		final int numDiffs = 5;
		RecentChangesService.SimpleDiff expectedDiff = new RecentChangesService.SimpleDiff();
        expectedDiff.setRemovedText("diff2");
        expectedDiff.setReplacementText("REPLACEMENT");

        var changeStorage = RecentChangesService.getInstance();

        var diffs = new ArrayList<RecentChangesService.SimpleDiff>();
        for (int i = 0; i < numDiffs; i++) {
            var diff = new RecentChangesService.SimpleDiff();
            String removedText = "diff"+i;
            diff.setRemovedText(removedText);
            diff.setReplacementText(
                    Objects.equals(removedText, expectedDiff.getRemovedText()) ?
                            expectedDiff.getReplacementText() :
                            "wrong");
            changeStorage.addChange(diff);
        }

        // Act.
        var result = changeStorage.getDiffMatchingRemovedText(expectedDiff.getRemovedText());

        // Assert.
        assertEquals(result, expectedDiff);
    }
    @Test
    void getDiffMatchingRemovedTextReturnsNullOnNoMatch() {
        // Arrange.
		final int numDiffs = 5;

        var changeStorage = RecentChangesService.getInstance();

        var diffs = new ArrayList<RecentChangesService.SimpleDiff>();
        for (int i = 0; i < numDiffs; i++) {
            var diff = new RecentChangesService.SimpleDiff();
            String removedText = "diff"+i;
            diff.setRemovedText(removedText);
            diff.setReplacementText("text");
            changeStorage.addChange(diff);
        }

        // Act.
        var result = changeStorage.getDiffMatchingRemovedText("DOES NOT EXIST");

        // Assert.
        assertNull(result);
    }
    @Test
    void getDiffMatchingRemovedTextReturnsNullOnEvictedDiff() {
        // Arrange.
		final int numDiffs = RecentChangesService.QUEUE_SIZE + 3;
        RecentChangesService.SimpleDiff expectedDiff = new RecentChangesService.SimpleDiff();
        expectedDiff.setRemovedText("diff2");
        expectedDiff.setReplacementText("REPLACEMENT");

        var changeStorage = RecentChangesService.getInstance();

        var diffs = new ArrayList<RecentChangesService.SimpleDiff>();
        for (int i = 0; i < numDiffs; i++) {
            var diff = new RecentChangesService.SimpleDiff();
            String removedText = "diff"+i;
            diff.setRemovedText(removedText);
            diff.setReplacementText(
                    Objects.equals(removedText, expectedDiff.getRemovedText()) ?
                            expectedDiff.getReplacementText() :
                            "wrong");
            changeStorage.addChange(diff);
        }

        // Act.
        var result = changeStorage.getDiffMatchingRemovedText(expectedDiff.getRemovedText());

        // Assert.
        assertNull(result);
    }
}

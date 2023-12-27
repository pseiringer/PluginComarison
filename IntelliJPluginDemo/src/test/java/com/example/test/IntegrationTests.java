package com.example.test;

import com.example.intellijplugindemo.services.RecentChangesService;
import com.example.intellijplugindemo.services.RecentChangesSettingsService;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class IntegrationTests extends BasePlatformTestCase {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RecentChangesService.getInstance().reset();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void simpleChangeDocumentListenerAddsSimpleChangeToRecentChangesService() throws InterruptedException {
        //open file in editor
        myFixture.configureByFile("TestSandbox.java");

        //replace selected text
        myFixture.type("TestReplacement");

        CountDownLatch lock = new CountDownLatch(1);
        AtomicReference<Deque<RecentChangesService.SimpleDiff>> foundChanges = new AtomicReference<>();

        //schedule a check after change has surely completed
        var taskWaitTime = RecentChangesSettingsService.getInstance().getDebounceTimeMs() + 1000;
        new Timer().schedule(new TimerTask() {
                                 @Override
                                 public void run() {
                                     foundChanges.set(RecentChangesService.getInstance().getRecentChanges());
                                     lock.countDown();
                                 }
                             },
                taskWaitTime
        );

        //wait for the timerTask to be finished
        lock.await(taskWaitTime + 1000, TimeUnit.MILLISECONDS);

        //see if a change has been found
        assertNotNull(foundChanges.get());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testdata";
    }
}

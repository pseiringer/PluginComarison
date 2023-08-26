package com.example.test;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.*;

import static com.intellij.testFramework.LightPlatformCodeInsightTestCase.*;
import static org.junit.jupiter.api.Assertions.*;

public class PopupDialogActionTest extends BasePlatformTestCase {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void firstTest() {
        assertEquals(1,1);
    }
    @Test
    void secondTest() {
        assertEquals(1,1);
    }
    @Test
    void thirdTest() {
        //open file in editor
        myFixture.configureByFile("someFile.xml");

        Runnable r = () -> {
            //commands to run on the open editor
            myFixture.renameElementAtCaret("newTagName");
        };

        //execute specified commands
        WriteCommandAction.runWriteCommandAction(getProject(), r);

        //check if editor content equals "someExpectedFile" content
        myFixture.checkResultByFile("someExpectedFile.xml");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testdata";
    }
}

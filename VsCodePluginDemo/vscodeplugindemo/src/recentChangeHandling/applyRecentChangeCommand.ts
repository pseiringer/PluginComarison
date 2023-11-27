
import * as vscode from 'vscode';

import { RecentChangeStorage, SimpleDiff } from './recentChangeStorage';

export class ApplyRecentChangeCommand {

    private readonly changes: RecentChangeStorage;

    constructor(changesStorage: RecentChangeStorage) {
        this.changes = changesStorage;
    }

    // applies the most recent fitting change for the word at the current position
    public applyRecentChange(editor: vscode.TextEditor, editBuilder: vscode.TextEditorEdit) {
        console.log("apply recent change");

        // check conditions
        if (editor === undefined){
            // no valid editor found
            console.log("no editor active");
            return;
        }

        // get the currently relevant range in the text
        let selectedRange = editor.document.getWordRangeAtPosition(editor.selection.anchor);
        if (selectedRange === undefined || !selectedRange.isSingleLine){
            // no valid word at the current position
            console.log("no selectable word at position");
            return;
        }

        // get the word at the selected range
        let selectedText = editor.document.getText(selectedRange);
        // try to find a matching change
        let foundDiff = this.changes.findMatchingChange(selectedText);
        if (foundDiff === undefined){
            // no change found
            console.log("no matching change detected");
            vscode.window.showInformationMessage("No matching change detected");
            return;
        }

        console.log("matching diff found");
        console.log(`replacing '${foundDiff.removedText}' with '${foundDiff.replacementText}'`);
        
        //calculate indices
        let idxInText = selectedText.indexOf(foundDiff.removedText);
        let replacementStart = new vscode.Position(
            selectedRange.start.line,
            selectedRange.start.character + idxInText);
        let replacementEnd = new vscode.Position(
            replacementStart.line,
            replacementStart.character + foundDiff.removedText.length);
        let replacementRange = new vscode.Range(replacementStart, replacementEnd);
        
        //replace calculated range
        editBuilder.replace(replacementRange, (foundDiff?.replacementText)??"");
    }


}
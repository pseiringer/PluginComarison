
import * as vscode from 'vscode';

import { RecentChangeStorage, SimpleDiff } from './recentChangeStorage';

export class ApplyRecentChangeCommand {

    private readonly changes: RecentChangeStorage;

    constructor(changesStorage: RecentChangeStorage) {
        this.changes = changesStorage;
    }

    public applyRecentChange(editor: vscode.TextEditor, editBuilder: vscode.TextEditorEdit) {
        console.log("apply recent change");

        //check conditions
        if (editor === undefined){
            console.log("no editor active");
            return;
        }

        let selectedRange = editor.document.getWordRangeAtPosition(editor.selection.anchor);
        if (selectedRange === undefined || !selectedRange.isSingleLine){
            console.log("no selectable word at position");
            return;
        }

        let selectedText = editor.document.getText(selectedRange);
        let foundDiff = this.changes.findMatchingChange(selectedText);
        if (foundDiff === undefined){
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
        // editor.edit((editBuilder) => {
        //     editBuilder.replace(replacementRange, (foundDiff?.replacementText)??"");
        // },{
        //     undoStopBefore: true, 
        //     undoStopAfter: false
        // });
    }


}
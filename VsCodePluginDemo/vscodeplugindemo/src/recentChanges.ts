
import * as vscode from 'vscode';
import { diff_match_patch } from 'diff-match-patch';
import { EvictingQueue } from './evictingQueue';

export class RecentChangeHandler {

    private readonly debounceTime = 750;
    private readonly debounce: (event: vscode.TextDocumentChangeEvent) => void;
    private isTyping: boolean = false;

    private textBeforeChange: string = "";

    private readonly dmp: diff_match_patch;

    private recentChanges: EvictingQueue<SimpleDiff> = new EvictingQueue(10);

    constructor() {
        this.debounce = this.createDebouncedChangeHandler(this.doneTyping, 1000);

        this.dmp = new diff_match_patch();
        this.dmp.Diff_Timeout = 3;
        // this.dmp.Diff_EditCost = 4;

        // let timer: NodeJS.Timeout;
        // this.debounce = (event: vscode.TextDocumentChangeEvent) => {
        //     clearTimeout(timer);
        //     timer = setTimeout(() => {
        //         this.doneTyping(event);
        //     }, this.debounceTime);
        // };
    }

    public handleOpenDocument(document: vscode.TextDocument){
        //document before first change == document after opening
        if(!this.isTyping){
            this.textBeforeChange = document.getText();
        }
    }

    public handleChange(event: vscode.TextDocumentChangeEvent) {
        if (!this.isTyping){
            //beginning of new change
            console.log("startTyping");
            //current state of document (textBeforeChange) has already been recorded 
            //after the last change or when opening the document
        }
        this.isTyping = true;
        this.debounce.call(this, event);
    }

    private doneTyping(event: vscode.TextDocumentChangeEvent){
        this.isTyping = false;
        console.log("done typing");

        let currentText = event.document.getText();
        let diffs = this.dmp.diff_main(this.textBeforeChange, currentText);

        this.dmp.diff_cleanupSemantic(diffs);

        let changes = diffs.filter(diff => diff[0] !== 0); //diff.operation != Operation.EQUAL
            
        if (changes.length === 2
            && changes.some(diff => diff[0] === 1) //diff.operation == Operation.INSERT
            && changes.some(diff => diff[0] === -1)){ //diff.operation == Operation.DELETE
            //simple change (insert + delete) detected
            //insert the simplified diff into recent changes
            console.log("simple change detected");

            let simpleDiff = new SimpleDiff();
            changes.forEach(change => {
                if(change[0] === 1) {
                    simpleDiff.replacementText = change[1];
                } else {
                    simpleDiff.removedText = change[1];
                }
            });
            this.recentChanges.enqueue(simpleDiff);
            console.log(this.recentChanges.toString());
        }
        else {
            console.log("no simple change found");
        }

        //text before next change == text after current change            
        this.textBeforeChange = currentText;
    }

    public applyRecentChange() {
        console.log("apply recent change");

        let editor = vscode.window.activeTextEditor;
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
        let foundDiff = this.findMatchingDiff(selectedText);
        if (foundDiff === undefined){
            console.log("no matching change detected");
            return;
        }

        console.log("matching diff found");
        console.log(`replacing '${foundDiff.removedText}' with '${foundDiff.replacementText}'`);
        
        let idxInText = selectedText.indexOf(foundDiff.removedText);
        let replacementStart = new vscode.Position(
            selectedRange.start.line,
            selectedRange.start.character + idxInText);
        let replacementEnd = new vscode.Position(
            replacementStart.line,
            replacementStart.character + foundDiff.removedText.length);
        let replacementRange = new vscode.Range(replacementStart, replacementEnd);
        
        editor.edit((editBuilder) => {
            editBuilder.replace(replacementRange, (foundDiff?.replacementText)??"");
        },{
            undoStopBefore: true, 
            undoStopAfter: false
        });
    }

    private findMatchingDiff(text: string): SimpleDiff | undefined {
        let queuedChanges = this.recentChanges.getData();
        for(let diff of queuedChanges){
            if(text.includes(diff.removedText)){
                return diff;
            }
        }
        return undefined;
    }

    private createDebouncedChangeHandler(callback: (event: vscode.TextDocumentChangeEvent) => void, debounceTime: number): (event: vscode.TextDocumentChangeEvent) => void {
        let timer: NodeJS.Timeout;
        return (event: vscode.TextDocumentChangeEvent) => {
            console.log("resetting timer...");
            clearTimeout(timer);
            timer = setTimeout(() => {callback.call(this, event);}, debounceTime);
        };
    }

}

class SimpleDiff {
    removedText: string;
    replacementText: string;

    public toString(): string {
        return `{${this.removedText},${this.replacementText}}`;
    }
}
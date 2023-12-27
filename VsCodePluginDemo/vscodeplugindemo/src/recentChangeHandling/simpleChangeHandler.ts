
import * as vscode from 'vscode';
import { diff_match_patch, Diff } from 'diff-match-patch';
import { RecentChangeStorage, SimpleDiff } from './recentChangeStorage';
import { RecentChangesSettings } from '../settings/recentChangesSettings';

export class SimpleChangeHandler {

    private readonly debounce: (event: vscode.TextDocumentChangeEvent, debounceTime: number) => void;
    private isTyping: boolean = false;

    private textBeforeChange: string = "";

    private readonly dmp: diff_match_patch;

    private readonly changes: RecentChangeStorage;

    constructor(changesStorage: RecentChangeStorage) {
        // create the debounced function to call doneTyping
        this.debounce = this.createDebouncedChangeHandler(this.doneTyping);

        // initialize the diff_match_patch algorithm
        this.dmp = new diff_match_patch();
        this.dmp.Diff_Timeout = 3;
        // this.dmp.Diff_EditCost = 4;

        this.changes = changesStorage;
    }

    // creates a function, that calls the given callback with a debounced delay
    private createDebouncedChangeHandler(callback: (event: vscode.TextDocumentChangeEvent) => void): 
            (event: vscode.TextDocumentChangeEvent, debounceTime: number) => void {
        // create the timer
        let timer: NodeJS.Timeout;
        // create the debounce function
        return (event: vscode.TextDocumentChangeEvent, debounceTime: number) => {
            // reset the timer
            // console.log("resetting timer...");
            clearTimeout(timer);
            // reinitialize the timer with the given debounce time
            timer = setTimeout(() => {callback.call(this, event);}, debounceTime);
        };
    }

    // stores the text before change on open document events
    public handleOpenDocument(document: vscode.TextDocument){
        if (vscode.window.activeTextEditor === undefined){
            // no active text editor -> ignore document
            return;
        }
        if(!this.isTyping){
            // document before first change <-> document after opening
            // store the current text so it can be
            // compared to after a change is complete
            this.textBeforeChange = document.getText();
        }
    }

    // stores the text before change on change editor events
    public handleChangeEditor(e: vscode.TextEditor | undefined){
        if (e !== undefined){
            // another editor has gained focus
            // store the current text so it can be
            // compared to after a change is complete
            this.textBeforeChange = e.document.getText();
        }
    }

    // starts a debounce timer to call doneTyping on document change events
    public handleChange(event: vscode.TextDocumentChangeEvent) {
        if (vscode.window.activeTextEditor === undefined){
            // no active text editor -> ignore changes
            return;
        }
        if (!this.isTyping){
            // beginning of new change
            // current state of document (textBeforeChange) has already been recorded 
            // after the last change or when opening the document/editor
            this.isTyping = true;
        }
        // create a debounced call to doneTyping
        this.debounce.call(this, event, RecentChangesSettings.getDebounceTimeFromSettings());
    }

    // records a change after it has been completed
    private doneTyping(event: vscode.TextDocumentChangeEvent){
        // a change has been completed
        this.isTyping = false;

        let currentText = event.document.getText();

        // record a change if one is found
        let foundChange = this.findSimpleChange(this.textBeforeChange, currentText);
        if (foundChange !== undefined){
            this.changes.addRecentChange(foundChange);
        }
        
        // text before next change == text after current change            
        this.textBeforeChange = currentText;
    }

    // returns a simple change if one can be found
    private findSimpleChange(previousText: string, currentText: string): SimpleDiff | undefined {
        // calculate all diffs

        // // calculate fully accurate diff and clean up to simplify
        // // sadly the cleanupSemantic feature does not work properly
        // // cleanupSemantic does not work -> using word mode instead
        // let diffs = this.dmp.diff_main(previousText, currentText);        
        // this.dmp.diff_cleanupSemantic(diffs);

        // use custom word mode instead
        let diffs = this.diff_wordMode(previousText, currentText);

        // filter all equal parts
        let changes = diffs.filter(diff => diff[0] !== 0); //diff.operation != Operation.EQUAL
            
        // see if remaining diffs are a simple change
        if (changes.length === 2
            && changes.some(diff => diff[0] === 1) // diff.operation == Operation.INSERT
            && changes.some(diff => diff[0] === -1)){ // diff.operation == Operation.DELETE
            // simple change (insert + delete) detected
            // insert the simplified diff into recent changes
            // console.log("simple change found");
            let simpleDiff = new SimpleDiff();
            changes.forEach(diff => {
                if(diff[0] === 1) { // diff.operation == Operation.INSERT
                    simpleDiff.replacementText = diff[1];
                } else {
                    simpleDiff.removedText = diff[1];
                }
            });
            return simpleDiff;
        }
        else {
            // no simple change found
            // console.log("no simple change found");
            return undefined;
        }
    }

    

    /** Modified copy of diff_lineMode from original diff-match-patch documentation */
    private diff_wordMode(text1: string, text2: string): Diff[] {
        let a = this.diff_wordsToChars(text1, text2);
        let wordText1 = a.chars1;
        let wordText2 = a.chars2;
        let wordArray = a.wordArray;
        let diffs = this.dmp.diff_main(wordText1, wordText2, false);
        this.dmp.diff_charsToLines_(diffs, wordArray);
        return diffs;
    }

    /** Modified copy of diff_linesToChars_ from original diff-match-patch */
    private diff_wordsToChars = function(text1: string, text2: string) {
        let wordArray: string[] = [];
        let wordHash: {[x: string]: number} = {};
    
        wordArray[0] = '';
    
        function diff_wordsToCharsMunge(text: string) {
            let chars = '';

            let wordStart = 0;
            let wordEnd = -1;
            
            let wordArrayLength = wordArray.length;
            function diff_addToken(tokenText: string){
                if (wordHash.hasOwnProperty ? wordHash.hasOwnProperty(tokenText) :
                    (wordHash[tokenText] !== undefined)) {
                    chars += String.fromCharCode(wordHash[tokenText]);
                } else {
                    if (wordArrayLength == maxWords) {
                        // Bail out at 65535 because
                        // String.fromCharCode(65536) == String.fromCharCode(0)
                        tokenText = text.substring(wordStart);
                        wordEnd = text.length;
                    }
                    chars += String.fromCharCode(wordArrayLength);
                    wordHash[tokenText] = wordArrayLength;
                    wordArray[wordArrayLength++] = tokenText;
                }
            }
            while (wordEnd < text.length - 1) {
                // Modified here to look for word boundaries, not just newlines
                // find idx of next word bound
                const WORD_BOUND: RegExp = /\W/; //TODO check if word bound can be improved
                let foundIndex = text.slice(wordStart).search(WORD_BOUND);
                if (foundIndex < 0) {
                    wordEnd = text.length;
                    let word = text.substring(wordStart, wordEnd);
                    diff_addToken(word);
                    break;
                }
                else{
                    wordEnd = wordStart + foundIndex;
                    if (wordStart !== wordEnd) {
                        let word = text.substring(wordStart, wordEnd);
                        diff_addToken(word);
                    }
                    let punct = text[wordEnd];
                    diff_addToken(punct);
                    wordStart = wordEnd + 1;
                }
            }
            return chars;
        }
        // Allocate 2/3rds of the space for text1, the rest for text2.
        let maxWords = 40000;
        let chars1 = diff_wordsToCharsMunge(text1);
        maxWords = 65535;
        let chars2 = diff_wordsToCharsMunge(text2);
        return {chars1: chars1, chars2: chars2, wordArray: wordArray};
    }
}
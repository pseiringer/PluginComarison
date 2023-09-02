
import * as vscode from 'vscode';
import { diff_match_patch, Diff } from 'diff-match-patch';
import { RecentChangeStorage, SimpleDiff } from './recentChangeStorage';

export class SimpleChangeHandler {

    private readonly debounceTime = 750;
    private readonly debounce: (event: vscode.TextDocumentChangeEvent) => void;
    private isTyping: boolean = false;

    private textBeforeChange: string = "";

    private readonly dmp: diff_match_patch;

    private readonly changes: RecentChangeStorage;

    constructor(changesStorage: RecentChangeStorage) {
        this.debounce = this.createDebouncedChangeHandler(this.doneTyping, 1000);

        this.dmp = new diff_match_patch();
        this.dmp.Diff_Timeout = 3;
        // this.dmp.Diff_EditCost = 4;

        this.changes = changesStorage;
    }

    private createDebouncedChangeHandler(callback: (event: vscode.TextDocumentChangeEvent) => void, debounceTime: number): (event: vscode.TextDocumentChangeEvent) => void {
        let timer: NodeJS.Timeout;
        return (event: vscode.TextDocumentChangeEvent) => {
            console.log("resetting timer...");
            clearTimeout(timer);
            timer = setTimeout(() => {callback.call(this, event);}, debounceTime);
        };
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

        let foundChange = this.findSimpleChange(this.textBeforeChange, currentText);
        if (foundChange !== undefined){
            this.changes.addRecentChange(foundChange);
        }
        
        //text before next change == text after current change            
        this.textBeforeChange = currentText;
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
                const WORD_BOUND: RegExp = /\W/;
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
    };

    private findSimpleChange(previousText: string, currentText: string): SimpleDiff | undefined {
        
        // calculate all diffs

        // // calculate fully accurate diff
        // // sadly the cleanupSemantic feature does not work properly
        // let diffs = this.dmp.diff_main(previousText, currentText);        
        // this.dmp.diff_cleanupSemantic(diffs);

        // use custom word mode instead
        //TODO improve word mode
        let diffs = this.diff_wordMode(previousText, currentText);

        // filter all equal parts
        let changes = diffs.filter(diff => diff[0] !== 0); //diff.operation != Operation.EQUAL
            
        // see if remaining diffs are a simple change
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
            return simpleDiff;
        }
        else {
            console.log("no simple change found");
            return undefined;
        }
    }

}
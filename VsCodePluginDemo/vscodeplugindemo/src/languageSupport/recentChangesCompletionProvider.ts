import * as vscode from 'vscode';
import { RecentChangeStorage, SimpleDiff } from '../recentChangeHandling/recentChangeStorage';

class RecentChangesCompletionProvider implements vscode.CompletionItemProvider {

    constructor(private changes: RecentChangeStorage){}

    // returns a Thenable of found changes fitting the current auto completion
    public provideCompletionItems(
            document: vscode.TextDocument, 
            position: vscode.Position, 
            token: vscode.CancellationToken)
                : Thenable<vscode.CompletionItem[]> {
            return new Promise(resolve => {
                // find changes fitting this auto completion
                var foundChanges = this.getFittingChanges(document, position);
                // resolve the Thenable to provide the completion items
                resolve(
                    foundChanges.map(
                        x => new vscode.CompletionItem(
                                    x.replacementText, 
                                    vscode.CompletionItemKind.Text
                                    )
                        )
                    );
            });
    }

    // get an array of changes suitable for autocompletion in the current position
    private getFittingChanges(document: vscode.TextDocument, position: vscode.Position): SimpleDiff[]{
        // get the currently relevant range in the text
        var selectedRange = document.getWordRangeAtPosition(position);
        if (selectedRange === undefined || !selectedRange.isSingleLine){
            // no selectable word at position
            return [];
        }

        // get the word at the selected range
        let selectedText = document.getText(selectedRange);
        // try to find a matching change
        let foundDiff = this.changes.findChange(diff => diff.replacementText.includes(selectedText));
        if (foundDiff === undefined){
            // no change found
            return [];
        }

        // matching change found
        return [foundDiff];
    }
}

// create the necessary subscription to activate the completion contributor
export function activateRecentChangesCompletionProvider(ctx: vscode.ExtensionContext, changes: RecentChangeStorage): void {
    ctx.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            {pattern:"**"},
            new RecentChangesCompletionProvider(changes), 
            '.', '\"' 
        )
    );
}
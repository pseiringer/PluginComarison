import * as vscode from 'vscode';
import { RecentChangeStorage, SimpleDiff } from '../recentChangeHandling/recentChangeStorage';

class RecentChangesCompletionProvider implements vscode.CompletionItemProvider {

    constructor(private changes: RecentChangeStorage){}

    public provideCompletionItems(
        document: vscode.TextDocument, position: vscode.Position, token: vscode.CancellationToken):
        Thenable<vscode.CompletionItem[]> {
            return new Promise(resolve => {
                var foundChanges = this.getFittingChanges(document, position);
                // resolve([{label:"xxx"}]);
                resolve(foundChanges.map(x => new vscode.CompletionItem(x.replacementText, vscode.CompletionItemKind.Text)));
            });
    }

    private getFittingChanges(document: vscode.TextDocument, position: vscode.Position): SimpleDiff[]{
        var selectedRange = document.getWordRangeAtPosition(position);
        if (selectedRange === undefined || !selectedRange.isSingleLine){
            // no selectable word at position
            return [];
        }

        let selectedText = document.getText(selectedRange);
        let foundDiff = this.changes.findChange(diff => diff.replacementText.includes(selectedText));
        if (foundDiff === undefined){
            // no matching change detected
            return [];
        }

        // matching diff found
        return [foundDiff];
    }
}

export function activateRecentChangesCompletionProvider(ctx: vscode.ExtensionContext, changes: RecentChangeStorage): void {
    ctx.subscriptions.push(
        vscode.languages.registerCompletionItemProvider(
            {pattern:"**"}, new RecentChangesCompletionProvider(changes), '.', '\"'));
}
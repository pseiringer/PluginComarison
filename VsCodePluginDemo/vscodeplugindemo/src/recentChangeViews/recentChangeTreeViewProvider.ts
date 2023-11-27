import * as vscode from 'vscode';
import { SimpleDiff, RecentChangeStorage } from '../recentChangeHandling/recentChangeStorage';

export class RecentChangeTreeViewProvider implements vscode.TreeDataProvider<SimpleDiffTreeItem> {

    constructor(private changes: RecentChangeStorage) {
        // add eventlistener to refresh the tree when the data changes
        changes.addEventListener(changes.storageChangedEventName, () => {
            this.refresh();
        });
    }

    // returns the TreeItem representation of a SimpleDiffTreeItem
    getTreeItem(element: SimpleDiffTreeItem): vscode.TreeItem {
        return element;
    }

    // gets all child elements of a SimpleDiffTreeItem
    getChildren(element?: SimpleDiffTreeItem): Thenable<SimpleDiffTreeItem[]> {
        // check if there are any changes to be shown
        if (!this.changes.hasChanges()) {
            // nothing to display
            vscode.window.showInformationMessage('No recent changes');
            return Promise.resolve([]);
        }

        if (element) {
            // the element was a change
            // return the removed and replacement elements
            return Promise.resolve([
                new SimpleDiffTreeItem(undefined, element.getRemovedLabel(), true, true),
                new SimpleDiffTreeItem(undefined, element.getReplacementLabel(), false, true)
            ]);
        } else {
            // the element was the root of the tree
            // return all changes (starting with the most recent)
            return Promise.resolve(
                this.changes.getAllChanges()
                    .slice()
                    .reverse()
                    .map(x => new SimpleDiffTreeItem(x, undefined, undefined, false)));
        }
    }

    // event to notify VS Code when the data changes
    private _onDidChangeTreeData: vscode.EventEmitter<SimpleDiffTreeItem | undefined | null | void> = new vscode.EventEmitter<SimpleDiffTreeItem | undefined | null | void>();
    readonly onDidChangeTreeData: vscode.Event<SimpleDiffTreeItem | undefined | null | void> = this._onDidChangeTreeData.event;

    // reload the tree with the current data
    refresh(): void {
        this._onDidChangeTreeData.fire();
    }
}

class SimpleDiffTreeItem extends vscode.TreeItem {
    constructor(
        private readonly diff: SimpleDiff | undefined,
        private readonly labelText: string | undefined,
        private readonly isRemoval: boolean | undefined,
        private readonly isLeafNode: boolean,
    ) {
        // check if configuration is correct
        // non leaf nodes only have a diff
        // leaf nodes only have labelText and IsRemoval
        if ((isLeafNode && 
                (diff !== undefined 
                || labelText === undefined 
                || isRemoval === undefined))
            || !isLeafNode && 
                (diff === undefined 
                || labelText !== undefined
                || isRemoval !== undefined)) {
            throw new Error("invalid configuration");
        }

        super(
            // label
            // leaf nodes display the labelText
            // non leaf nodes show "removed -> replacement"
            ((isLeafNode && labelText) ?
                labelText :
                ((!isLeafNode && diff) ?
                    `'${diff.removedText}' -> '${diff.replacementText}'` :
                    "")),
            // collapsible state
            // leaf nodes are in state none (they have no children)
            // other nodes are collapsed
            ((isLeafNode && labelText) ?
                vscode.TreeItemCollapsibleState.None :
                vscode.TreeItemCollapsibleState.Collapsed)
        );

        // set the icon and tooltip
        if (!isLeafNode && diff) {
            this.iconPath = new vscode.ThemeIcon("arrow-swap");
            this.tooltip = `SimpleChange replaces the text '${diff.removedText}' with '${diff.replacementText}'`;
        }
        else if (isLeafNode && labelText && (isRemoval !== undefined)) {
            if (isRemoval) {
                this.iconPath = new vscode.ThemeIcon("diff-remove");
            }
            else {
                this.iconPath = new vscode.ThemeIcon("diff-insert");
            }
            this.tooltip = labelText;
        }
    }

    // returns the text removed in a change (only for non leaf nodes)
    public getRemovedLabel(): string {
        if (this.isLeafNode) {
            throw new Error("invalid configuration");
        }
        return `Removes '${this.diff?.removedText ?? ""}'`;
    }
    // returns the text replaced in a change (only for non leaf nodes)
    public getReplacementLabel(): string {
        if (this.isLeafNode) {
            throw new Error("invalid configuration");
        }
        return `Adds '${this.diff?.replacementText ?? ""}'`;
    }
}
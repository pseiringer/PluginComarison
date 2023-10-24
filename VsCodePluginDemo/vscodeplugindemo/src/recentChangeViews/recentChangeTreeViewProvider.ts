import * as vscode from 'vscode';
import { SimpleDiff, RecentChangeStorage } from '../recentChangeHandling/recentChangeStorage';

export class RecentChangeTreeViewProvider implements vscode.TreeDataProvider<SimpleDiffTreeItem> {
  
  constructor(private changes: RecentChangeStorage) {}

  getTreeItem(element: SimpleDiffTreeItem): vscode.TreeItem {
    return element;
  }

  getChildren(element?: SimpleDiffTreeItem): Thenable<SimpleDiffTreeItem[]> {
    if (!this.changes.hasChanges()) {
      vscode.window.showInformationMessage('No recent changes');
      return Promise.resolve([]);
    }

    if (element) {
      return Promise.resolve([
        new SimpleDiffTreeItem(undefined, element.getRemovedLabel(), true, true),
        new SimpleDiffTreeItem(undefined, element.getReplacementLabel(), false, true)
      ]);
    } else {
      return Promise.resolve(
        this.changes.getAllChanges()
          .slice()
          .reverse()
          .map(x => new SimpleDiffTreeItem(x, undefined, undefined, false)));
    }
  }

  private _onDidChangeTreeData: vscode.EventEmitter<SimpleDiffTreeItem | undefined | null | void> = new vscode.EventEmitter<SimpleDiffTreeItem | undefined | null | void>();
  readonly onDidChangeTreeData: vscode.Event<SimpleDiffTreeItem | undefined | null | void> = this._onDidChangeTreeData.event;

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
    if ((isLeafNode && (diff !== undefined || labelText === undefined || isRemoval === undefined))
      || !isLeafNode && (diff === undefined || labelText !== undefined || isRemoval !== undefined)){
      throw new Error("invalid configuration");
    }

    super(
      ((isLeafNode && labelText) ?
        labelText :
        ((!isLeafNode && diff) ? 
          `'${diff.removedText}' -> '${diff.replacementText}'` : 
          "")),
      ((isLeafNode && labelText) ? 
        vscode.TreeItemCollapsibleState.None :
        vscode.TreeItemCollapsibleState.Collapsed)
    );

    if(!isLeafNode && diff){
      this.iconPath = new vscode.ThemeIcon("arrow-swap");
      this.tooltip = `SimpleChange replaces the text '${diff.removedText}' with '${diff.replacementText}'`;
    }
    else if (isLeafNode && labelText && (isRemoval !== undefined)){
      if (isRemoval){
        this.iconPath = new vscode.ThemeIcon("diff-remove");   
      }
      else {
        this.iconPath = new vscode.ThemeIcon("diff-insert");   
      }   
      this.tooltip = labelText;
    }
  }

  public getRemovedLabel(): string{
    if (this.isLeafNode){
      throw new Error("invalid configuration");
    }
    return `Removes '${this.diff?.removedText??""}'`;
  }
  public getReplacementLabel(): string{
    if (this.isLeafNode){
      throw new Error("invalid configuration");
    }
    return `Adds '${this.diff?.replacementText??""}'`;
  }
}
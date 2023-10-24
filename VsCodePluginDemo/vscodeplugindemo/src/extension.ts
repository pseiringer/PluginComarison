import * as vscode from 'vscode';

import { SimpleChangeHandler } from './recentChangeHandling/simpleChangeHandler';
import { RecentChangeStorage } from './recentChangeHandling/recentChangeStorage';
import { ApplyRecentChangeCommand } from './recentChangeHandling/applyRecentChangeCommand';
import { RecentChangeTreeViewProvider } from './recentChangeViews/recentChangeTreeViewProvider';
import { activateRecentChangesCompletionProvider } from './languageSupport/recentChangesCompletionProvider';

let changeStorage = new RecentChangeStorage();
let simpleChangeHandler = new SimpleChangeHandler(changeStorage);
let applyRecentChangeCommand = new ApplyRecentChangeCommand(changeStorage);
let recentChangeTreeViewProvider = new RecentChangeTreeViewProvider(changeStorage);

export function activate(context: vscode.ExtensionContext) {

	console.log('"vscodeplugindemo" is now active!');
	// TODO add setting for debounce time and maybe queue size

	// TODO remove
	// // register some simple commands
	// context.subscriptions.push(vscode.commands.registerCommand('vscodeplugindemo.helloWorld', () => {
	// 	vscode.window.showInformationMessage('Hello World from VsCodePluginDemo!');
	// }));
	// context.subscriptions.push(vscode.commands.registerCommand('vscodeplugindemo.returnOne', () => {
	// 	vscode.window.showInformationMessage('returnOne was called!');
	// 	return 1;
	// }));


	// register SimpleChangeHandler for recognizing changes
	context.subscriptions.push(vscode.workspace.onDidOpenTextDocument(
		simpleChangeHandler.handleOpenDocument, 
		simpleChangeHandler
	));
	context.subscriptions.push(vscode.workspace.onDidChangeTextDocument(
		simpleChangeHandler.handleChange, 
		simpleChangeHandler
	));

	if (vscode.window.activeTextEditor !== undefined){
		// handle the document that has been opened at startup
		simpleChangeHandler.handleOpenDocument(vscode.window.activeTextEditor.document);
	}

	// register ApplyRecentChangeCommand for applying previously found changes
	// since it is registered as TextEditorCommand it can only be activated when there is 
	// an editor in active.
	context.subscriptions.push(vscode.commands.registerTextEditorCommand('vscodeplugindemo.applyRecentChange', 
		applyRecentChangeCommand.applyRecentChange, 
		applyRecentChangeCommand
	));

	// register tree view
	vscode.window.createTreeView(
		'recentChangeView',
		{
			treeDataProvider: recentChangeTreeViewProvider
		}
	);
	// add ways to refresh tree
	vscode.commands.registerCommand('recentChangeView.refreshEntry', () =>
		recentChangeTreeViewProvider.refresh()
	);
	changeStorage.addEventListener(changeStorage.storageChangedEventName, () => {		
		recentChangeTreeViewProvider.refresh();
	});

	// activate auto completion
	activateRecentChangesCompletionProvider(context, changeStorage);
}

// This method is called when your extension is deactivated
export function deactivate() {}

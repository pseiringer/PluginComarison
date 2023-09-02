import * as vscode from 'vscode';

import { SimpleChangeHandler } from './recentChangeHandling/simpleChangeHandler';
import { RecentChangeStorage } from './recentChangeHandling/recentChangeStorage';
import { ApplyRecentChangeCommand } from './recentChangeHandling/applyRecentChangeCommand';
import { RecentChangeTreeViewProvider } from './recentChangeViews/recentChangeTreeViewProvider';

let changeStorage = new RecentChangeStorage();
let simpleChangeHandler = new SimpleChangeHandler(changeStorage);
let applyRecentChangeCommand = new ApplyRecentChangeCommand(changeStorage);
let recentChangeTreeViewProvider = new RecentChangeTreeViewProvider(changeStorage);

export function activate(context: vscode.ExtensionContext) {

	console.log('Congratulations, your extension "vscodeplugindemo" is now active!');

	// register some simple commands
	context.subscriptions.push(vscode.commands.registerCommand('vscodeplugindemo.helloWorld', () => {
		vscode.window.showInformationMessage('Hello World from VsCodePluginDemo!');
	}));
	context.subscriptions.push(vscode.commands.registerCommand('vscodeplugindemo.returnOne', () => {
		vscode.window.showInformationMessage('returnOne was called!');
		return 1;
	}));

	// register SimpleChangeHandler for recognizing changes
	vscode.workspace.onDidOpenTextDocument(simpleChangeHandler.handleOpenDocument, simpleChangeHandler);
	vscode.workspace.onDidChangeTextDocument(simpleChangeHandler.handleChange, simpleChangeHandler);

	if (vscode.window.activeTextEditor !== undefined){
		simpleChangeHandler.handleOpenDocument.call(simpleChangeHandler, vscode.window.activeTextEditor.document);
	}

	// register ApplyRecentChangeCommand for applying previously found changes
	context.subscriptions.push(vscode.commands.registerCommand('vscodeplugindemo.applyRecentChange', 
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
	vscode.commands.registerCommand('recentChangeView.refreshEntry', () =>
		recentChangeTreeViewProvider.refresh()
	);
}

// This method is called when your extension is deactivated
export function deactivate() {}

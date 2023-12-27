import * as vscode from 'vscode';

import { SimpleChangeHandler } from './recentChangeHandling/simpleChangeHandler';
import { RecentChangeStorage, SimpleDiff } from './recentChangeHandling/recentChangeStorage';
import { ApplyRecentChangeCommand } from './recentChangeHandling/applyRecentChangeCommand';
import { RecentChangeTreeViewProvider } from './recentChangeViews/recentChangeTreeViewProvider';
import { activateRecentChangesCompletionProvider } from './languageSupport/recentChangesCompletionProvider';

let changeStorage: RecentChangeStorage;
let simpleChangeHandler: SimpleChangeHandler;
let applyRecentChangeCommand: ApplyRecentChangeCommand;
let recentChangeTreeViewProvider: RecentChangeTreeViewProvider;

export function activate(context: vscode.ExtensionContext) {

	console.log('"vscodeplugindemo" is now active!');
	
	// create the storage and all change handlers
	changeStorage = new RecentChangeStorage(context);
	simpleChangeHandler = new SimpleChangeHandler(changeStorage);
	applyRecentChangeCommand = new ApplyRecentChangeCommand(changeStorage);
	recentChangeTreeViewProvider = new RecentChangeTreeViewProvider(changeStorage);

	// register SimpleChangeHandler for recognizing document/text changes
	context.subscriptions.push(vscode.window.onDidChangeActiveTextEditor(
		simpleChangeHandler.handleChangeEditor, 
		simpleChangeHandler	
	));
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

	// register ApplyRecentChangeCommand for applying previously found changes.
	// since it is registered as TextEditorCommand it can only be activated 
	// when there is an editor active.
	context.subscriptions.push(vscode.commands.registerTextEditorCommand('vscodeplugindemo.applyRecentChange', 
		applyRecentChangeCommand.applyRecentChange, 
		applyRecentChangeCommand
	));
	
	// register getRecentChanges command so the storage can be read out in the integration tests
	context.subscriptions.push(vscode.commands.registerCommand('vscodeplugindemo.getRecentChanges', 
		() => {
			var result = changeStorage?.getAllChanges() ?? undefined;
			// console.log(result);
			return result;
		}
	));

	// register tree view
	vscode.window.createTreeView(
		'recentChangesView',
		{
			treeDataProvider: recentChangeTreeViewProvider
		}
	);
	
	// activate/register auto completion
	activateRecentChangesCompletionProvider(context, changeStorage);
}

// This method is called when your extension is deactivated
export function deactivate() {}

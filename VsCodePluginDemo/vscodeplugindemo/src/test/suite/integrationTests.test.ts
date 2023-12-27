import * as assert from 'assert';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';

import expect = require('expect.js');
import { RecentChangesSettings } from '../../settings/recentChangesSettings';
import { SimpleDiff } from '../../recentChangeHandling/recentChangeStorage';

suite('Integration Test Suite', () => {
	vscode.window.showInformationMessage('running tests...');

	const testFileContent = 
`public class Main {
    public static int world;
    public static boolean someValue2;
    public static String someValue3;
    public static int some_value_4;
    
    private static void main(String[] args) {
    
        System.out.println("Hello VSCODE!");

        System.out.println(someValue2);
        System.out.println(someValue3);
        System.out.println(some_value_4);
    }
}`;

	test('simpleChangeHandler adds simple changes to recentChangeStorage', (done) => {
		var taskWaitTime = RecentChangesSettings.getDebounceTimeFromSettings() + 1000;
		vscode.workspace.openTextDocument({content: testFileContent})
			.then(document => { // open a new document
				return vscode.window.showTextDocument(document);
			})
			.then(editor => { // change the text
				var range = editor.document.validateRange(
					new vscode.Range(
						new vscode.Position(8,30), 
						new vscode.Position(8,32)
					)
				);
				return editor.edit(editBuilder => {
					editBuilder.replace(range,"TEST");
				});
			})
			.then(() => new Promise(resolve => setTimeout(resolve, taskWaitTime))) // wait until the plugin has recognized the change
			.then(() => vscode.commands.executeCommand('vscodeplugindemo.getRecentChanges')) // get all changes
			.then((value) => value as SimpleDiff[] | undefined) // cast the result to the correct type
			.then((value) => { // cleanup (close the opened document and return the previous value)
				return vscode.commands.executeCommand('workbench.action.closeActiveEditor')
							.then(() => value);
			}) 
			.then( // assert the result
				result => { //fulfilled
					assert.ok(
						result !== undefined && 
						result.length === 1 &&
						result[0].removedText === 'Hello' &&
						result[0].replacementText === 'HeTESTo'
					);
					done();
				},
				error => { //rejected
					assert.ok(false); // assertion error is thrown, no need to call done()
					//done();
				}
			);
	}).timeout(RecentChangesSettings.getDebounceTimeFromSettings() + 10000);

	// test('Sample test', () => {
	// 	assert.strictEqual(-1, [1, 2, 3].indexOf(5));
	// 	assert.strictEqual(-1, [1, 2, 3].indexOf(0));
	// });

	
	// test('Async test', (done) => {
	// 	vscode.commands.executeCommand('vscodeplugindemo.returnOne')
	// 		.then((val) => {
	// 			//fulfilled
	// 			assert.strictEqual(1, val);
	// 			done();
	// 		});
	// });

	// test('third test', () => {
	// 	assert.strictEqual(-1, [1, 2, 3].indexOf(5));
	// });

	// [1,2,3].forEach((value) => {
	// 	test(`parameterized test [${value}]`, () => {
	// 		assert.ok([0,1,2,3,4].includes(value));
	// 	});
	// });
});

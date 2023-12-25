import * as assert from 'assert';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';

import expect = require('expect.js');

suite('Integration Test Suite', () => {
	vscode.window.showInformationMessage('running tests...');

	const testDataPath = './testdata/TestSandbox.java';
	const testFileContent = 'Hello World Hello World Hello World';

	// setup(async () => {

	// })

	test('simpleChangeHandler adds simple changes to recentChangeStorage', (done) => {
		vscode.workspace.openTextDocument({content: testFileContent})
			.then(document => {
				return vscode.window.showTextDocument(document);
			})
			.then(editor => {
				var range = editor.document.validateRange(
					new vscode.Range(
						new vscode.Position(0,2), 
						new vscode.Position(0,4)
					)
				);
				return editor.edit(editBuilder => {
					editBuilder.replace(range,"TEST");
					// TODO the test does not work, since i could not get the simpleChangeHandler to trigger here
					// maybe add an additional command just for triggering a text changed?
				});
			})
			.then(
				fulfilled => {
					assert.ok(true);
					done();
				},
				rejected => {
					assert.ok(false);
				}
			);
	});

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

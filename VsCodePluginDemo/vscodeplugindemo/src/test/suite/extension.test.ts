import * as assert from 'assert';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as pluginDemo from '../../extension';

suite('Extension Test Suite', () => {
	vscode.window.showInformationMessage('Start all tests.');

	test('Sample test', () => {
		assert.strictEqual(-1, [1, 2, 3].indexOf(5));
		assert.strictEqual(-1, [1, 2, 3].indexOf(0));
	});

	
	test('Async test', (done) => {
		vscode.commands.executeCommand('vscodeplugindemo.returnOne')
			.then((val) => {
				//fulfilled //TODO this is not called? maybe command needs to specify that it returns sth?
				assert.strictEqual(1, val);
				done();
			});
		
	});
	test('third test', () => {
		assert.strictEqual(-1, [1, 2, 3].indexOf(5));
	});
});

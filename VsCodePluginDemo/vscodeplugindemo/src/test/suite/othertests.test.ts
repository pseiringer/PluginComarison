import * as assert from 'assert';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';

suite('Demo Test Suite', () => {
	vscode.window.showInformationMessage('Start all tests.');

	test('Sample test', () => {
		assert.strictEqual(-1, [1, 2, 3].indexOf(5));
		assert.strictEqual(-1, [1, 2, 3].indexOf(0));
	});

	
	test('Async test', (done) => {
		vscode.commands.executeCommand('vscodeplugindemo.returnOne')
			.then((val) => {
				//fulfilled
				assert.strictEqual(1, val);
				done();
			});
		
	});
	test('third test', () => {
		assert.strictEqual(-1, [1, 2, 3].indexOf(5));
	});

	[1,2,3].forEach((value) => {
		test(`parameterized test [${value}]`, () => {
			assert.ok([0,1,2,3,4].includes(value));
		});
	})
});

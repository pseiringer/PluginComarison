import * as assert from 'assert';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as pluginDemo from '../../extension';
import { RecentChangeStorage, SimpleDiff } from '../../recentChangeHandling/recentChangeStorage';

suite('recentChangeStorage Unit Tests', () => {
	test('addRecentChange dispatches storageChangedEvent', () => {
		// Arrange.
		let changeStorage = new RecentChangeStorage();

		let eventCalled = false;
		changeStorage.addEventListener(changeStorage.storageChangedEventName, () => {
			eventCalled = true;
		});

		let diff = new SimpleDiff();

		// Act.
		changeStorage.addRecentChange(diff);

		// Assert.
		assert.ok(eventCalled, 'The event callback has not been called');
	});
	
});

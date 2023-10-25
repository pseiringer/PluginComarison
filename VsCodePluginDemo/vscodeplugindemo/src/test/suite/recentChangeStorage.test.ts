import * as assert from 'assert';

// You can import and use all API from the 'vscode' module
// as well as import your extension to test it
import * as vscode from 'vscode';
import * as pluginDemo from '../../extension';
import { RecentChangeStorage, SimpleDiff } from '../../recentChangeHandling/recentChangeStorage';

import expect = require('expect.js');

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

	test('addRecentChange adds SimpleDiff', () => {
		// Arrange.
		const numDiffs = 5;

		// let changeStorage = new RecentChangeStorage(numDiffs + 1);
		let changeStorage = new RecentChangeStorage();
		changeStorage.resizeChangesQueue(numDiffs + 1);

		let diffs: SimpleDiff[] = [];
		for (let i = 0; i < numDiffs; i++) {
			let diff = new SimpleDiff();
			diff.removedText = `diff${i}`;
			diffs.push(diff);
		}

		// Act.
		for (let i = 0; i < numDiffs; i++) {
			changeStorage.addRecentChange(diffs[i]);
		}

		// Assert.		
		assert.deepStrictEqual(changeStorage.getAllChanges().sort(), diffs.sort());
	});	

	test('addRecentChange evicts old SimpleDiff', () => {
		// Arrange.
		const numDiffs = 5;
		const queueSize = 2;
		let evictedDiffs = numDiffs - queueSize;

		let changeStorage = new RecentChangeStorage();
		changeStorage.resizeChangesQueue(queueSize);
		// let changeStorage = new RecentChangeStorage(queueSize);

		let diffs: SimpleDiff[] = [];
		let removedDiffs: SimpleDiff[] = [];
		let containedDiffs: SimpleDiff[] = [];
		for (let i = 0; i < numDiffs; i++) {
			let diff = new SimpleDiff();
			diff.removedText = `diff${i}`;
			diffs.push(diff);
			if (i < evictedDiffs) {
				removedDiffs.push(diff);
			}
			else {
				containedDiffs.push(diff);
			}
		}

		// Act.
		for (let i = 0; i < numDiffs; i++) {
			changeStorage.addRecentChange(diffs[i]);
		}

		// Assert.		
		let result = changeStorage.getAllChanges().sort();
		assert.deepStrictEqual(result, containedDiffs.sort());
		removedDiffs.forEach(diff => {
			expect(result).to.not.contain(diff);
		});
	});
	
	test('findMatchingChange returns fitting SimpleDiff on match', () => {
		// Arrange.
		const numDiffs = 5;
		const expectedDiff: SimpleDiff = {
			removedText: 'diff2',
			replacementText: 'REPLACEMENT'
		};

		let changeStorage = new RecentChangeStorage();
		changeStorage.resizeChangesQueue(numDiffs + 1);
		// let changeStorage = new RecentChangeStorage(numDiffs + 1);

		let diffs: SimpleDiff[] = [];
		for (let i = 0; i < numDiffs; i++) {
			let diff = new SimpleDiff();
			let removedText = `diff${i}`;
			diff.removedText = removedText;
			diff.replacementText = removedText === expectedDiff.removedText ? expectedDiff.replacementText : 'wrong';
			changeStorage.addRecentChange(diff);
		}

		// Act.
		let result = changeStorage.findMatchingChange(expectedDiff.removedText);

		// Assert.
		assert.deepEqual(result, expectedDiff);
	});
	
	test('findMatchingChange returns undefined on no match', () => {
		// Arrange.
		const numDiffs = 5;

		let changeStorage = new RecentChangeStorage();
		changeStorage.resizeChangesQueue(numDiffs + 1);
		// let changeStorage = new RecentChangeStorage(numDiffs + 1);

		let diffs: SimpleDiff[] = [];
		for (let i = 0; i < numDiffs; i++) {
			let diff = new SimpleDiff();
			let removedText = `diff${i}`;
			diff.removedText = removedText;
			diff.replacementText = 'text';
			changeStorage.addRecentChange(diff);
		}

		// Act.
		let result = changeStorage.findMatchingChange('DOES NOT EXIST');

		// Assert.
		assert.equal(result, undefined);
	});
	
	test('findMatchingChange returns undefined on evicted diff', () => {
		// Arrange.
		const numDiffs = 5;
		const queueSize = 2;
		const expectedDiff: SimpleDiff = {
			removedText: 'diff2',
			replacementText: 'REPLACEMENT'
		};

		let changeStorage = new RecentChangeStorage();
		changeStorage.resizeChangesQueue(queueSize);
		// let changeStorage = new RecentChangeStorage(queueSize);

		let diffs: SimpleDiff[] = [];
		for (let i = 0; i < numDiffs; i++) {
			let diff = new SimpleDiff();
			let removedText = `diff${i}`;
			diff.removedText = removedText;
			diff.replacementText = removedText === expectedDiff.removedText ? expectedDiff.replacementText : 'wrong';
			changeStorage.addRecentChange(diff);
		}

		// Act.
		let result = changeStorage.findMatchingChange(expectedDiff.removedText);

		// Assert.
		assert.equal(result, undefined);
	});
});

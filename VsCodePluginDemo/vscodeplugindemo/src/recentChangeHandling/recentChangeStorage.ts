import * as vscode from 'vscode';

import { EvictingQueue } from '../util/evictingQueue';
import { RecentChangesSettings } from '../settings/recentChangesSettings';

export class RecentChangeStorage extends EventTarget{

    public readonly storageChangedEventName: string = "storageChanged";

    // event that is dispatched when an entry is added or
    // removed from the storage
    private storageChangedEvent: Event = new Event(this.storageChangedEventName);

    private recentChanges: EvictingQueue<SimpleDiff>;

    constructor(context: vscode.ExtensionContext | undefined = undefined) {
        super();
        // subscribe to be notified when the QueueSize setting is changed
        context?.subscriptions.push(vscode.workspace.onDidChangeConfiguration(evt => {
                if (evt.affectsConfiguration(
                        `${RecentChangesSettings.settingsSection}.${RecentChangesSettings.settingQueueSize}`
                    )){
                    this.resizeChangesQueue(RecentChangesSettings.getQueueSizeFromSettings());
                }
            }, 
            this
        ));
        // initialize the evicting queue
        this.recentChanges = new EvictingQueue(RecentChangesSettings.getQueueSizeFromSettings());
    }

    // enqueues a new change if it was not previously contained
    public addRecentChange(change: SimpleDiff): void {
        if (this.containsChange(change))
            return;
        this.recentChanges.enqueue(change);
        this.dispatchEvent(this.storageChangedEvent);
    }
    
    // find the first change where the removed text is contained in the parameter text
    public findMatchingChange(text: string): SimpleDiff | undefined {
        return this.findChange(diff => text.includes(diff.removedText));
    }
    
    // find the first change for which isValid returns true
    public findChange(isValid: (diff: SimpleDiff) => boolean): SimpleDiff | undefined {
        let queuedChanges = this.recentChanges.getData().slice().reverse();
        for(let diff of queuedChanges){
            if(isValid(diff)){
                return diff;
            }
        }
        return undefined;
    }

    // checks whether a SimpleDiff that equals the given diff is already contained in the storage
    private containsChange(diff: SimpleDiff): boolean {
        return this.findChange(
            x => 
                x.removedText === diff.removedText && 
                x.replacementText === diff.replacementText
        ) != undefined;
    }

    // returns whether there are any changes stored currently
    public hasChanges(): boolean {
        return this.recentChanges.getData().length > 0;
    }

    // returns all currently stored changes
    public getAllChanges(): SimpleDiff[]{
        return this.recentChanges.getData();
    }

    // replaces the current evicting queue with a new queue of capacity size
    public resizeChangesQueue(size: number): void {
        // create new queue
        let newQueue = new EvictingQueue<SimpleDiff>(size);
        // transfer data to new queue
        this.recentChanges.getData().forEach(change => {
            newQueue.enqueue(change);
        });
        // replace the old queue
        this.recentChanges = newQueue;
        this.dispatchEvent(this.storageChangedEvent);
    }
}

export class SimpleDiff {
    removedText: string;
    replacementText: string;

    public toString(): string {
        return `{${this.removedText},${this.replacementText}}`;
    }
}
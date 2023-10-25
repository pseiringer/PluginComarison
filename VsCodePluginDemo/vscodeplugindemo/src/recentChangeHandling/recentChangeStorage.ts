import * as vscode from 'vscode';

import { EvictingQueue } from '../util/evictingQueue';
import { RecentChangesSettings } from '../settings/recentChangesSettings';

export class RecentChangeStorage extends EventTarget{

    public readonly storageChangedEventName: string = "storageChanged";

    private storageChangedEvent: Event = new Event(this.storageChangedEventName);

    private recentChanges: EvictingQueue<SimpleDiff>;

    constructor(context: vscode.ExtensionContext | undefined = undefined) {
        super();
        context?.subscriptions.push(vscode.workspace.onDidChangeConfiguration(evt => {
                if (evt.affectsConfiguration(
                        `${RecentChangesSettings.settingsSection}.${RecentChangesSettings.settingQueueSize}`
                    )){
                    this.resizeChangesQueue(RecentChangesSettings.getQueueSizeFromSettings());
                }
            }, 
            this
        ));
        this.recentChanges = new EvictingQueue(RecentChangesSettings.getQueueSizeFromSettings());
    }

    public addRecentChange(change: SimpleDiff): void {
        this.recentChanges.enqueue(change);
        this.dispatchEvent(this.storageChangedEvent);
    }
    
    public findMatchingChange(text: string): SimpleDiff | undefined {
        return this.findChange(diff => text.includes(diff.removedText));
    }
    
    public findChange(isValid: (diff: SimpleDiff) => boolean): SimpleDiff | undefined {
        let queuedChanges = this.recentChanges.getData().slice().reverse();
        for(let diff of queuedChanges){
            if(isValid(diff)){
                return diff;
            }
        }
        return undefined;
    }

    public hasChanges(): boolean {
        return this.recentChanges.getData().length > 0;
    }

    public getAllChanges(): SimpleDiff[]{
        return this.recentChanges.getData();
    }

    public resizeChangesQueue(size: number): void {
        let newQueue = new EvictingQueue<SimpleDiff>(size);
        this.recentChanges.getData().forEach(change => {
            newQueue.enqueue(change);
        });
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
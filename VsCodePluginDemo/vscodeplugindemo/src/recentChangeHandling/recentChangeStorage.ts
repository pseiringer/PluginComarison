import { EvictingQueue } from '../util/evictingQueue';

export class RecentChangeStorage extends EventTarget{

    public readonly storageChangedEventName: string = "storageChanged";

    private recentChanges: EvictingQueue<SimpleDiff> = new EvictingQueue(10);
    private storageChangedEvent: Event = new Event(this.storageChangedEventName);

    public addRecentChange(change: SimpleDiff): void {
        this.recentChanges.enqueue(change);
        this.dispatchEvent(this.storageChangedEvent);
    }
    
    public findMatchingChange(text: string): SimpleDiff | undefined {
        let queuedChanges = this.recentChanges.getData();
        for(let diff of queuedChanges){
            if(text.includes(diff.removedText)){
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
}

export class SimpleDiff {
    removedText: string;
    replacementText: string;

    public toString(): string {
        return `{${this.removedText},${this.replacementText}}`;
    }
}
import { EvictingQueue } from '../util/evictingQueue';

export class RecentChangeStorage extends EventTarget{

    public readonly storageChangedEventName: string = "storageChanged";
    private storageChangedEvent: Event = new Event(this.storageChangedEventName);

    private recentChanges: EvictingQueue<SimpleDiff>;

    constructor(queueSize: number = 10) {
        super();
        this.recentChanges = new EvictingQueue(queueSize);
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
}

export class SimpleDiff {
    removedText: string;
    replacementText: string;

    public toString(): string {
        return `{${this.removedText},${this.replacementText}}`;
    }
}

export class EvictingQueue<T>{
    private data: T[];
    private capacity: number;

    constructor(capacity: number){
        this.data = [];
        this.capacity = capacity;
    }

    public enqueue(element: T): void {
        this.data.push(element);
        while(this.data.length > this.capacity){
            this.data.shift();
        }
    }
    public dequeue(): T | undefined {
        return this.data.shift();
    }
    public toString(): string {
        return this.data.toString();
    }
    public getData(): T[] {
        return this.data;
    }

    //test evicting queue
}
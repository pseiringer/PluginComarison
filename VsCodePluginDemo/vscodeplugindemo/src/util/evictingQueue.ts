
export class EvictingQueue<T>{
    private data: T[];
    private capacity: number;

    constructor(capacity: number){
        this.data = [];
        this.capacity = capacity;
    }

    // adds the element at the end of the queue
    // if the queue exceeds its capacity, elements
    // at the front of the queue are dequeued
    public enqueue(element: T): void {
        this.data.push(element);
        while(this.data.length > this.capacity){
            this.data.shift();
        }
    }

    // removes and returns the element at the front of the queue
    public dequeue(): T | undefined {
        return this.data.shift();
    }

    // prints the content of the queue
    public toString(): string {
        return this.data.toString();
    }

    // returns the content of the queue as an array
    public getData(): T[] {
        return this.data;
    }
}
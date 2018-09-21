
export class Dto{
    constructor(public name: string, public value: string) {}


    equals(another: Dto): boolean {
        return another !== null && another.name === this.name && another.value === this.value;
    }
}

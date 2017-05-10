
export class Result{
    value: string;
    isOk: boolean;

    constructor(value: string, ok: boolean){
        this.value = value;
        this.isOk = ok;
    }
}

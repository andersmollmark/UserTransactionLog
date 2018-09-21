import {Dto} from "./dto";

export class ColumnFilter {

    name: string = '';
    value: string = '';

    constructor(private columnVals: Dto) {
        this.name = columnVals.name;
        this.value = columnVals.value;
    }
}

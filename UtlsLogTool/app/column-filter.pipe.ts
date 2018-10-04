import {Pipe, PipeTransform} from "@angular/core";
import {UtlsLog} from "./utls-log";
import {ColumnFilter} from "./column-filter";
import {AppConstants} from "./app.constants";

@Pipe({
    name: "columnFilter"
})
export class ColumnFilterPipe implements PipeTransform {

    constructor() {
    }

    transform(logs: UtlsLog[], columnFilter: ColumnFilter): UtlsLog[] {
        if(logs !== null && logs.length > 0) {
            if(AppConstants.STR_ALL === columnFilter.value) {
                return logs;
            }
            return logs.filter(log => log[columnFilter.name] === columnFilter.value);
        }

        return [];
    }


}
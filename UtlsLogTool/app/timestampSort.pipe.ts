import * as _ from "lodash";
import {AppSettings} from "./app.settings";
import {Pipe, PipeTransform} from "@angular/core";
import {UtlsLog} from "./log";

@Pipe({
    name: "timestampSort"
})
export class TimeStampSortPipe implements PipeTransform {

    constructor() {

    }

    transform(array: any[], sort: string): any {
        if (sort && sort.length > 0 && !_.isNil(array)) {
            if(AppSettings.TIMESTAMP_SORT_ASC === sort){
                return array.sort((a: UtlsLog, b: UtlsLog) => {
                   return b.timestamp - a.timestamp
                });
            }
            else if(AppSettings.TIMESTAMP_SORT_DESC === sort){
                return array.sort((a: UtlsLog, b: UtlsLog) => {
                   return a.timestamp - b.timestamp
                });
            }
            return _.filter(array, row=>row.name.indexOf(sort) > -1);
        }
        return array;
    }


}

import * as _ from "lodash";
import {AppSettings} from "./app.settings";
import {Pipe, PipeTransform} from "@angular/core";
import {UtlsLog} from "./log";

@Pipe({
    name: "dataFilter"
})
export class DataFilterPipe implements PipeTransform {

    transform(array: any[], query: string): any {
        if (query && !_.isNil(array)) {
            if(AppSettings.TIMESTAMP_SORT_ASC === query){
                return array.sort((a: UtlsLog, b: UtlsLog) => {
                   return b.timestamp - a.timestamp
                });
            }
            else if(AppSettings.TIMESTAMP_SORT_DESC === query){
                return array.sort((a: UtlsLog, b: UtlsLog) => {
                   return a.timestamp - b.timestamp
                });
            }
            return _.filter(array, row=>row.name.indexOf(query) > -1);
        }
        return array;
    }
}
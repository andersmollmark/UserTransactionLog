import * as _ from "lodash";
import {AppSettings} from "./app.settings";
import {Pipe, PipeTransform} from "@angular/core";
import {UtlsLog} from "./log";
import {SortingObject} from "./sortingObject";

@Pipe({
    name: "columnSort"
})
export class ColumnSortPipe implements PipeTransform {

    constructor() {

    }

    transform(array: any[], sortObject: SortingObject): any {
        var result = array;
        if (sortObject && sortObject.isOk() && !_.isNil(array)) {
            if(AppSettings.COLUMN_SORT_ASC === sortObject.sortorder){
                if(sortObject.isTimestamp()){
                    result = array.sort((a: UtlsLog, b: UtlsLog) => {
                        return b.timestamp - a.timestamp
                    });
                }
                else{
                    result = _.orderBy(array, sortObject.sortname, "asc")
                }

            }
            else if(AppSettings.COLUMN_SORT_DESC === sortObject.sortorder){
                if(sortObject.isTimestamp()){
                    result = array.sort((a: UtlsLog, b: UtlsLog) => {
                        return a.timestamp - b.timestamp
                    });
                }
                else{
                    result = _.orderBy(array, sortObject.sortname, "desc")
                }
            }
        }
        return result;
    }


}
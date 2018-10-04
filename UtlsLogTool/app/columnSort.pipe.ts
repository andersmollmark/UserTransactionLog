import * as _ from "lodash";
import {AppConstants} from "./app.constants";
import {Pipe, PipeTransform} from "@angular/core";
import {UtlsLog} from "./utls-log";
import {SortingObject} from "./sortingObject";

@Pipe({
    name: "columnSort"
})
export class ColumnSortPipe implements PipeTransform {

    constructor() {

    }

    transform(array: any[], sortObject: SortingObject): any {
        let result = array;
        if (sortObject && sortObject.isOk() && !_.isNil(array)) {
            if(AppConstants.COLUMN_SORT_ASC === sortObject.sortorder){
                if(sortObject.isTimestamp()){
                    result = array.sort((a: UtlsLog, b: UtlsLog) => {
                        return b.timestamp - a.timestamp;
                    });
                }
                else{
                    result = _.orderBy(array, sortObject.sortname, "asc");
                }

            }
            else if(AppConstants.COLUMN_SORT_DESC === sortObject.sortorder){
                if(sortObject.isTimestamp()){
                    result = array.sort((a: UtlsLog, b: UtlsLog) => {
                        return a.timestamp - b.timestamp;
                    });
                }
                else{
                    result = _.orderBy(array, sortObject.sortname, "desc");
                }
            }
        }
        return result;
    }


}
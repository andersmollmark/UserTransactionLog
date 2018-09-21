import * as _ from "lodash";
import {AppConstants} from "./app.constants";
import {Pipe, PipeTransform} from "@angular/core";
import {TimeFilterService} from "./timefilter.service";
import {UtlsFileService} from "./utls-file.service";
import {UtlsLog} from "./log";

@Pipe({
    name: "dataFilter"
})
export class DataFilterPipe implements PipeTransform {

    constructor(private filterService: TimeFilterService, private utlsFileService: UtlsFileService) {

    }

    transform(array: UtlsLog[], query: string): any {
        let resultArray = array;
        if (query && query.length > 0 && !_.isNil(array)) {
            if(query.includes(AppConstants.TIMESTAMP_FILTER_FROM)) {
                if (this.filterService.getSelectedTimefilterFrom() && this.filterService.getSelectedTimefilterFrom().getOriginTimestamp() > 0
                    && this.filterService.getSelectedTimefilterTo() && this.filterService.getSelectedTimefilterTo().getOriginTimestamp() > 0) {
                    resultArray = this.sortLogrowsInTime(array);
                }
                else if (this.filterService.getSelectedTimefilterFrom()) {
                    let from = this.filterService.getSelectedTimefilterFrom().getOriginTimestamp();
                    console.log('from:' + from);
                    resultArray = _.filter(array, row => {
                        let timestamp = this.getTimestampForTimezone(row);
                        return timestamp >= from;
                    });
                }
                this.utlsFileService.createColumnFilteringValuesForLogs(resultArray);
            }
            else if(query.includes(AppConstants.TIMESTAMP_FILTER_TO)){
                if (this.filterService.getSelectedTimefilterFrom() && this.filterService.getSelectedTimefilterTo()) {
                    resultArray = this.sortLogrowsInTime(array);
                }
                else if(this.filterService.getSelectedTimefilterTo()){
                    let to = this.filterService.getSelectedTimefilterTo().getOriginTimestamp();
                    console.log('to:' + to);
                    resultArray = _.filter(array, row => {
                        let timestamp = this.getTimestampForTimezone(row);
                        return timestamp <= to;
                    });
                }
                this.utlsFileService.createColumnFilteringValuesForLogs(resultArray);
            }
            else{
                resultArray = _.filter(array, row => row.name.indexOf(query) > -1);
            }
        }
        return resultArray;
    }

    private sortLogrowsInTime(array: UtlsLog[]): UtlsLog[] {
        let from = this.filterService.getSelectedTimefilterFrom().getOriginTimestamp();
        let to = this.filterService.getSelectedTimefilterTo().getOriginTimestamp();

        return array.filter(row => {
            let timestamp = this.getTimestampForTimezone(row);
            return timestamp >= from && timestamp <= to;
        });

    }


    private getTimestampForTimezone(logRow: UtlsLog): number {
        return logRow.timestampWithTimezone.get(this.filterService.getCurrentTimezone());
    }




}
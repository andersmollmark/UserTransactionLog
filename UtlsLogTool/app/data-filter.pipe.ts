import * as _ from "lodash";
import {AppConstants} from "./app.constants";
import {Pipe, PipeTransform} from "@angular/core";
import {TimeFilterService} from "./timefilter.service";
import {UtlsFileService} from "./utls-file.service";

@Pipe({
    name: "dataFilter"
})
export class DataFilterPipe implements PipeTransform {

    constructor(private filterService: TimeFilterService, private utlsFileService: UtlsFileService) {

    }

    transform(array: any[], query: string): any {
        let resultArray = array;
        if (query && query.length > 0 && !_.isNil(array)) {
            if(query.includes(AppConstants.TIMESTAMP_FILTER_FROM)) {
                if (this.filterService.getSelectedTimefilterFrom() && this.filterService.getSelectedTimefilterFrom().value
                    && this.filterService.getSelectedTimefilterTo() && this.filterService.getSelectedTimefilterTo().value) {
                    let from = this.filterService.getSelectedTimefilterFrom().value.getTime();
                    let to = this.filterService.getSelectedTimefilterTo().value.getTime();
                    console.log('from:' + from + ' to:' + to);
                    resultArray = _.filter(array, row => row.timestamp >= from && row.timestamp <= to);
                }
                else if (this.filterService.getSelectedTimefilterFrom()) {
                    let from = this.filterService.getSelectedTimefilterFrom().value.getTime();
                    console.log('from:' + from);
                    resultArray = _.filter(array, row => row.timestamp >= from);
                }
                this.utlsFileService.createColumnFilteringValuesForLogs(resultArray);
            }
            else if(query.includes(AppConstants.TIMESTAMP_FILTER_TO)){
                if (this.filterService.getSelectedTimefilterFrom() && this.filterService.getSelectedTimefilterTo()) {
                    let from = this.filterService.getSelectedTimefilterFrom().value.getTime();
                    let to = this.filterService.getSelectedTimefilterTo().value.getTime();
                    console.log('from:' + from + ' to:' + to);
                    resultArray = _.filter(array, row => row.timestamp >= from && row.timestamp <= to);
                }
                else if(this.filterService.getSelectedTimefilterTo()){
                    let to = this.filterService.getSelectedTimefilterTo().value.getTime();
                    console.log('to:' + to);
                    resultArray = _.filter(array, row => row.timestamp <= to);
                }
                this.utlsFileService.createColumnFilteringValuesForLogs(resultArray);
            }
            else{
                resultArray = _.filter(array, row=>row.name.indexOf(query) > -1);
            }
        }
        return resultArray;
    }




}
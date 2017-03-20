import * as _ from "lodash";
import {AppSettings} from "./app.settings";
import {Pipe, PipeTransform} from "@angular/core";
import {UtlsLog} from "./log";
import {FilterService} from "./filter.service";

@Pipe({
    name: "dataFilter"
})
export class DataFilterPipe implements PipeTransform {

    constructor(private filterService: FilterService) {

    }

    transform(array: any[], query: string): any {
        let resultArray = array;
        if (query && query.length > 0 && !_.isNil(array)) {
            if(query.includes(AppSettings.TIMESTAMP_FILTER_FROM)) {
                if (this.filterService.getTimefilterFrom() && this.filterService.getTimefilterTo()) {
                    let from = this.filterService.getTimefilterFrom().getTime();
                    let to = this.filterService.getTimefilterTo().getTime();
                    console.log('from:' + from + ' to:' + to);
                    resultArray = _.filter(array, row => row.timestamp >= from && row.timestamp <= to);
                }
                else if (this.filterService.getTimefilterFrom()) {
                    let from = this.filterService.getTimefilterFrom().getTime();
                    console.log('from:' + from);
                    resultArray = _.filter(array, row => row.timestamp >= from);
                }
            }
            else if(query.includes(AppSettings.TIMESTAMP_FILTER_TO)){
                if (this.filterService.getTimefilterFrom() && this.filterService.getTimefilterTo()) {
                    let from = this.filterService.getTimefilterFrom().getTime();
                    let to = this.filterService.getTimefilterTo().getTime();
                    console.log('from:' + from + ' to:' + to);
                    resultArray = _.filter(array, row => row.timestamp >= from && row.timestamp <= to);
                }
                else if(this.filterService.getTimefilterTo()){
                    let to = this.filterService.getTimefilterTo().getTime();
                    console.log('to:' + to);
                    resultArray = _.filter(array, row => row.timestamp <= to);
                }
            }
            else{
                resultArray = _.filter(array, row=>row.name.indexOf(query) > -1);
            }
        }
        return resultArray;
    }




}
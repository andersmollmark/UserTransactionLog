import {Injectable} from "@angular/core";
import {SelectedDate} from "./selectedDate";

@Injectable()
export class TimeFilterService {

    private firstDateFromFile: Date;
    private lastDateFromFile: Date;

    private selectedTimefilterFrom: SelectedDate;
    private selectedTimefilterTo: SelectedDate;

    private lastSelectedTimefilterFrom: SelectedDate;
    private lastSelectedTimefilterTo: SelectedDate;

    private filterQuery: string = "";

    constructor() {
    }

    setFirstDateFromFile(firstDate: Date){
        this.firstDateFromFile = firstDate;
    }

    setLastDateFromFile(lastDate: Date){
        this.lastDateFromFile = lastDate;
    }

    resetTimefilter(){
        this.filterQuery = "";
        this.setSelectedTimefilterFrom(this.firstDateFromFile);
        this.setSelectedTimefilterTo(this.lastDateFromFile);
        this.setLastSelectedTimefilterFrom(this.firstDateFromFile);
        this.setLastSelectedTimefilterTo(this.lastDateFromFile);
    }

    setSelectedTimefilterFrom(from: Date){
        this.selectedTimefilterFrom = SelectedDate.getFromDate(from);
    }

    setSelectedTimefilterTo(to: Date){
        this.selectedTimefilterTo = SelectedDate.getFromDate(to);
    }

    getSelectedTimefilterFrom(): SelectedDate{
        return this.selectedTimefilterFrom;
    }

    getSelectedTimefilterTo(): SelectedDate{
        return this.selectedTimefilterTo;
    }

    setLastSelectedTimefilterFrom(from: Date){
        this.lastSelectedTimefilterFrom = SelectedDate.getFromDateParts(from);
    }

    setLastSelectedTimefilterTo(to: Date){
        this.lastSelectedTimefilterTo = SelectedDate.getFromDateParts(to);
    }

    getLastSelectedTimefilterFrom(): SelectedDate{
        return this.lastSelectedTimefilterFrom;
    }

    getLastSelectedTimefilterTo(): SelectedDate{
        return this.lastSelectedTimefilterTo;
    }

    setFilterQuery(query: string){
        this.filterQuery = query;
    }


    getFilterQuery(): string{
        return this.filterQuery;
    }

}
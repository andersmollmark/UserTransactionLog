import {Component, OnInit} from "@angular/core";
import {SelectedDate} from "./selectedDate";
import {TimeFilterService} from "./timefilter.service";
import {AppConstants} from "./app.constants";
@Component({
    selector: 'timefilter',
    templateUrl: './timefilter.component.html'
})
export class TimefilterComponent {

    showTo: boolean = false;
    showFrom: boolean = false;
    showFromTime: boolean = false;
    showToTime: boolean = false;
    ismeridian:boolean = true;
    fromTime: Date = new Date();
    toTime: Date = new Date();

    lastSelectedDateFrom: SelectedDate;
    lastSelectedDateTo: SelectedDate;
    selectedDateFrom: SelectedDate;
    selectedDateTo: SelectedDate;

    constructor(private timeFilterService: TimeFilterService){

    }

    ngDoCheck(){
        this.lastSelectedDateFrom = this.timeFilterService.getLastSelectedTimefilterFrom();
        this.lastSelectedDateTo = this.timeFilterService.getLastSelectedTimefilterTo();
        this.selectedDateFrom = this.timeFilterService.getSelectedTimefilterFrom();
        this.selectedDateTo = this.timeFilterService.getSelectedTimefilterTo();
    }

    getFromFilterDateString(): string{
        return this.timeFilterService.getSelectedTimefilterFrom() ?
            this.timeFilterService.getSelectedTimefilterFrom().asString() : '';
    }

    getToFilterDateString(): string{
        return this.timeFilterService.getSelectedTimefilterTo() ?
            this.timeFilterService.getSelectedTimefilterTo().asString() : '';
    }

    setShowFrom(){
        this.showFrom = true;
    }

    setShowFromTime(){
        this.showFromTime = true;
    }

    isShowFrom(){
        return this.showFrom;
    }

    isShowFromTime(){
        return this.showFromTime;
    }

    changeFrom(){
        if(this.selectedDateFrom){
            if(!this.selectedDateFrom.isSame(this.lastSelectedDateFrom)){
                this.timeFilterService.setLastSelectedTimefilterFrom(this.selectedDateFrom.value);
                this.showFrom = false;
                this.showFromTime = true;
            }
        }
    }

    changeFromTime(){
        if(this.fromTime && this.selectedDateFrom){
            if(!this.selectedDateFrom.isSameTime(this.fromTime)){
                this.selectedDateFrom.setTime(this.fromTime, 0);
            }
            this.timeFilterService.setSelectedTimefilterFrom(this.selectedDateFrom.value);
            this.timeFilterService.setFilterQuery(AppConstants.TIMESTAMP_FILTER_FROM.concat(this.selectedDateFrom.asString()));
            this.showFromTime = false;
        }
    }

    changeTo(){
        if(this.selectedDateTo){
            if(!this.selectedDateTo.isSame(this.lastSelectedDateTo)){
                this.timeFilterService.setLastSelectedTimefilterTo(this.selectedDateTo.value);
                this.showTo = false;
                this.showToTime = true;
            }
        }

    }

    changeToTime(){
        if(this.toTime && this.selectedDateTo){
            if(!this.selectedDateTo.isSameTime(this.toTime)){
                this.selectedDateTo.setTime(this.toTime, 59);
            }
            this.timeFilterService.setSelectedTimefilterTo(this.selectedDateTo.value);
            this.timeFilterService.setFilterQuery(AppConstants.TIMESTAMP_FILTER_TO.concat(this.selectedDateTo.asString()));
            this.showToTime = false;
        }
    }

    setShowTo(){
        this.showTo = true;
    }

    setShowToTime(){
        this.showToTime = true;
    }

    isShowTo(){
        return this.showTo;
    }


    isShowToTime(){
        return this.showToTime;
    }



}


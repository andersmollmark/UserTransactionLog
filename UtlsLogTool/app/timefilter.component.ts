import {Component, NgZone, OnDestroy, OnInit} from "@angular/core";
import {SelectedDate} from "./selectedDate";
import {TimeFilterService} from "./timefilter.service";
import {AppConstants} from "./app.constants";
import {Subscription} from "rxjs";

@Component({
    selector: 'timefilter',
    templateUrl: './timefilter.component.html'
})
export class TimefilterComponent implements OnInit, OnDestroy {

    showTo: boolean = false;
    showFrom: boolean = false;
    showFromTime: boolean = false;
    showToTime: boolean = false;
    ismeridian: boolean = true;
    fromTime: Date = new Date();
    toTime: Date = new Date();

    selectedDateFrom: SelectedDate = null;
    selectedDateTo: SelectedDate = null;

    fromString: string = '';
    toString: string = '';


    defaultDateTo: Date = new Date();

    private hourMode12Subscription: Subscription = null;
    private timefilterFromSubscription: Subscription = null;
    private timefilterToSubscription: Subscription = null;

    constructor(private timeFilterService: TimeFilterService, private zone: NgZone) {
    }

    ngOnInit(): void {
        this.hourMode12Subscription = this.timeFilterService.get12HourModeSubscription().subscribe(mode12Hour => {
            this.zone.run(() => this.ismeridian = mode12Hour);
        });

        this.timefilterFromSubscription = this.timeFilterService.subscribeSelectedFrom().subscribe(selectedDate => {
            this.zone.run(() => {
                if(selectedDate !== null){
                    this.selectedDateFrom = selectedDate;
                    this.fromTime = selectedDate.getValue();
                }

            });
        });

        this.timefilterToSubscription = this.timeFilterService.subscribeSelectedTo().subscribe(selectedDate => {
            this.zone.run(() => {
                if(selectedDate !== null){
                    this.selectedDateTo = selectedDate;
                    this.toTime = selectedDate.getValue();
                    // this.defaultDateTo = new Date(this.toTime.getTime());
                }
            });
        });
    }

    ngOnDestroy(): void {
        this.hourMode12Subscription.unsubscribe();
        this.timefilterFromSubscription.unsubscribe();
        this.timefilterToSubscription.unsubscribe();
    }

    ngDoCheck() {
        this.selectedDateFrom = this.timeFilterService.getSelectedTimefilterFrom();
        if (this.selectedDateFrom !== null) {
            this.fromString = this.selectedDateFrom.asString();
        }

        this.selectedDateTo = this.timeFilterService.getSelectedTimefilterTo();
        if (this.selectedDateTo !== null) {
            this.toString = this.selectedDateTo.asString();
        }
    }

    closeFrom(): void {
        if(this.shouldHourAndMinutesBeReset(this.showFromTime, this.fromTime, this.selectedDateFrom.getValue())) {
            this.selectedDateFrom.setTime(this.fromTime, this.fromTime.getSeconds());
        }
        this.showFrom = false;
        this.showFromTime = false;
    }

    closeTo(): void {
        if(this.shouldHourAndMinutesBeReset(this.showToTime, this.toTime, this.selectedDateTo.getValue())) {
            this.selectedDateTo.setTime(this.toTime, this.toTime.getSeconds());
        }

        this.showTo = false;
        this.showToTime = false;
    }

    private shouldHourAndMinutesBeReset(showTimePopup: boolean, time: Date, selectedDate: Date): boolean {
        return !showTimePopup && time.getHours() > 0 && selectedDate.getHours() === 0;
    }

    setShowFrom() {
        this.showFrom = true;
    }

    setShowFromTime() {
        this.showFromTime = true;
    }

    isShowFrom() {
        return this.showFrom;
    }

    isShowFromTime() {
        return this.showFromTime;
    }

    changeFrom() {
        if (this.selectedDateFrom) {
            if (this.selectedDateFrom.isChangedDate()) {
                this.selectedDateFrom.updateDates();
                this.showFrom = false;
                this.showFromTime = true;
            }
        }
    }

    changeFromTime() {
        if (this.fromTime && this.selectedDateFrom) {
            if (this.selectedDateFrom.isChangedTime(this.fromTime)) {
                this.timeFilterService.setFromTime(this.selectedDateFrom, this.fromTime);
                this.timeFilterService.setFilterQuery(AppConstants.TIMESTAMP_FILTER_FROM.concat(this.selectedDateFrom.asString()));
            }
            this.showFromTime = false;
        }
    }

    changeTo() {
        if (this.selectedDateTo) {
            if (this.selectedDateTo.isChangedDate()) {
                this.selectedDateTo.updateDates();
                this.showTo = false;
                this.showToTime = true;
            }
        }

    }

    changeToTime() {
        if (this.toTime && this.selectedDateTo) {
            if (this.selectedDateTo.isChangedTime(this.toTime)) {
                this.timeFilterService.setToTime(this.selectedDateTo, this.toTime);
                this.timeFilterService.setFilterQuery(AppConstants.TIMESTAMP_FILTER_TO.concat(this.selectedDateTo.asString()));
            }
            this.showToTime = false;
        }
    }

    setShowTo() {
        this.showTo = true;
    }

    setShowToTime() {
        this.showToTime = true;
    }

    isShowTo() {
        return this.showTo;
    }


    isShowToTime() {
        return this.showToTime;
    }


}


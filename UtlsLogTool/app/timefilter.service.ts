import {Injectable} from "@angular/core";
import {SelectedDate} from "./selectedDate";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {TimeHandler} from "./time-handler";
import {UtlsLog} from "./utls-log";

@Injectable()
export class TimeFilterService {

    private firstDateFromFile: Date = null;
    private lastDateFromFile: Date = null;

    private selectedTimefilterFrom: SelectedDate = null;
    private selectedTimefilterTo: SelectedDate = null;

    private filterQuery: string = "";

    private is12HourMode: boolean = false;

    private hourMode12: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    private selectedFromSubject: BehaviorSubject<SelectedDate> = new BehaviorSubject<SelectedDate>(null);
    private selectedToSubject: BehaviorSubject<SelectedDate> = new BehaviorSubject<SelectedDate>(null);

    private currentTimezone: string = '';
    private logsTimezoneId: string = '';


    constructor() {
    }

    set12HrMode(timezone: string): void {
        this.is12HourMode = true;
        this.updateTimeHandler(timezone);
        this.hourMode12.next(true);
    }

    set24HrMode(timezone: string): void {
        this.is12HourMode = false;
        this.updateTimeHandler(timezone);
        this.hourMode12.next(false);
    }

    private updateTimeHandler(timezone: string): void {
        let timeHandler = TimeHandler.getInstance();
        timeHandler.set12HourMode(this.is12HourMode);
        this.updateToAndFromDates(timezone);
    }

    private updateToAndFromDates(timezone: string): void {
        if (this.getSelectedTimefilterFrom() !== null) {
            this.setSelectedTimefilterFrom(this.selectedTimefilterFrom.changeDueToNewTimezone(this.is12HourMode, timezone));
            this.setSelectedTimefilterTo(this.selectedTimefilterTo.changeDueToNewTimezone(this.is12HourMode, timezone));
        }
    }

    setCurrentTimezone(timezone: string): void {
        this.currentTimezone = timezone;
    }

    getCurrentTimezone(): string {
        return this.currentTimezone;
    }

    setLogsTimezone(logsTimezone: string): void {
        this.logsTimezoneId = logsTimezone;
    }

    get12HourModeSubscription(): BehaviorSubject<boolean> {
        return this.hourMode12;
    }

    subscribeSelectedFrom(): BehaviorSubject<SelectedDate> {
        return this.selectedFromSubject;
    }

    subscribeSelectedTo(): BehaviorSubject<SelectedDate> {
        return this.selectedToSubject;
    }

    setFirstDateFromFile(firstDate: Date) {
        this.firstDateFromFile = firstDate;
    }

    setLastDateFromFile(lastDate: Date) {
        this.lastDateFromFile = lastDate;
    }


    createTimefilter(timezoneId: string) {
        this.filterQuery = "";
        console.log('timefilterService: createTimeFilter');
        this.setSelectedTimefilterFrom(SelectedDate.createOrigin(this.firstDateFromFile, this.is12HourMode, timezoneId, 0));
        this.setSelectedTimefilterTo(SelectedDate.createOrigin(this.lastDateFromFile, this.is12HourMode, timezoneId, 59));
    }

    setFromTime(from: SelectedDate, newValue: Date) {
        from.setTime(newValue, 0);
        this.setSelectedTimefilterFrom(from);
    }

    setToTime(to: SelectedDate, newValue: Date) {
        to.setTime(newValue, 59);
        this.setSelectedTimefilterTo(to);
    }


    setSelectedTimefilterFrom(from: SelectedDate) {
        this.selectedTimefilterFrom = from;
        this.selectedFromSubject.next(this.selectedTimefilterFrom);
    }

    setSelectedTimefilterTo(to: SelectedDate) {
        this.selectedTimefilterTo = to;
        this.selectedToSubject.next(this.selectedTimefilterTo);
    }

    getSelectedTimefilterFrom(): SelectedDate {
        return this.selectedTimefilterFrom;
    }

    getSelectedTimefilterTo(): SelectedDate {
        return this.selectedTimefilterTo;
    }

    setFilterQuery(query: string) {
        this.filterQuery = query;
    }


    getFilterQuery(): string {
        return this.filterQuery;
    }

    changeTimezone(newTimezone: string, activeLogs: UtlsLog[]): void {
        let timezoneHandler = TimeHandler.getInstance();
        this.setCurrentTimezone(newTimezone);
        timezoneHandler.changeTimezone(newTimezone, activeLogs);
        this.selectedTimefilterFrom = this.selectedTimefilterFrom.changeDueToNewTimezone(this.is12HourMode, newTimezone);
        this.selectedTimefilterTo = this.selectedTimefilterTo.changeDueToNewTimezone(this.is12HourMode, newTimezone);

        this.selectedFromSubject.next(this.selectedTimefilterFrom);
        this.selectedToSubject.next(this.selectedTimefilterTo);
    }

}
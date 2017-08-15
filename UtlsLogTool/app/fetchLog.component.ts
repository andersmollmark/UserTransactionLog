import {Component, Output, EventEmitter, Input, NgZone} from "@angular/core";
import {AppConstants} from "./app.constants";
import {UtlsFileService} from "./utls-file.service";
import {TimeFilterService} from "./timefilter.service";
import {SelectedDate} from "./selectedDate";
import moment = require("moment");
import {FetchLogParam} from "./fetchLogParam";

@Component({
    selector: 'fetchLog',
    templateUrl: './fetchLog.component.html'
})
export class FetchLogComponent {

    private timezonesDefault = '--- Select ---';
    public timezones = [];
    private chosenTimezone = this.timezonesDefault;

    @Input()
    showMe: boolean = false;

    @Output()
    isVisibleEvent: EventEmitter<boolean> = new EventEmitter<boolean>();

    @Output()
    fetchLogsEvent: EventEmitter<FetchLogParam> = new EventEmitter<FetchLogParam>();


    constructor(private timefilterService: TimeFilterService, private zone: NgZone) {
        let now = moment().toDate();
        let oneMonthAgo = moment().subtract(1, 'month').toDate();
        timefilterService.setFirstDateFromFile(oneMonthAgo);
        timefilterService.setLastDateFromFile(now);
        this.timefilterService.resetTimefilter();

        let favorite = localStorage.getItem(AppConstants.TIMEZONE_FAVORITE);

        let momentTz = require("moment-timezone");
        this.zone.run(() => {
            this.timezones = momentTz.tz.names();
            if(favorite){
                this.chosenTimezone = favorite;
            }
        });
    }

    fetchLogs(): void {
        if(this.chosenTimezone && this.chosenTimezone !== this.timezonesDefault){
            let from = this.timefilterService.getSelectedTimefilterFrom();
            let to = this.timefilterService.getSelectedTimefilterTo();
            localStorage.setItem(AppConstants.TIMEZONE_FAVORITE, this.chosenTimezone);
            this.fetch(from, to);
        }
    }

    close(): void {
        this.showMe = false;
        this.isVisibleEvent.emit(this.showMe);
    }

    choseTimezone(timezone) {
        console.log('chosen timezone=' + timezone);
        this.chosenTimezone = timezone;
    }

    private fetch(from: SelectedDate, to: SelectedDate): void {
        this.zone.run(() => {
            this.close();
            let result = new FetchLogParam();
            result.from = from.getValue();
            result.to = to.getValue();
            result.timezone = this.chosenTimezone;
            this.fetchLogsEvent.emit(result);
        });
    }

}


import {LogAction} from "./log-action";
import {AppComponent} from "./app.component";
import {UtlsLog} from "./utls-log";

export class ResetAllLogsAction implements LogAction {

    constructor(private appComponent: AppComponent) {}

    execute(logs: UtlsLog[]): void {
        this.appComponent.activeLogs = logs;
        this.appComponent.resetTimezone();
        this.appComponent.setTimeData(logs);
        this.appComponent.timeFilterService.createTimefilter(this.appComponent.currentTimezoneId);
    }
}
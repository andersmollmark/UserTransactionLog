import {LogAction} from "./log-action";
import {AppComponent} from "./app.component";
import {UtlsLog} from "./utls-log";

export class CreateAllLogsAction implements LogAction {

    constructor(private appComponent: AppComponent) {}

    execute(logs: UtlsLog[]): void {
        this.appComponent.setTimezones();
        this.appComponent.activeLogs = logs;
        this.appComponent.setTimeData(logs);
        this.appComponent.timeFilterService.createTimefilter(this.appComponent.currentTimezoneId);
    }
}
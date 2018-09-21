import {LogAction} from "./log-action";
import {AppComponent} from "./app.component";
import {UtlsLog} from "./log";

export class GetAllLogsAction implements LogAction {

    constructor(private appComponent: AppComponent) {}

    execute(logs: UtlsLog[]): void {
        this.appComponent.activeLogs = logs;
    }
}
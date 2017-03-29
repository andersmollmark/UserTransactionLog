import {Component, Output, EventEmitter, Input, NgZone} from "@angular/core";
import {AppConstants} from "./app.constants";
import {UtlsFileService} from "./utls-file.service";

@Component({
    selector: 'utlSettings',
    templateUrl: './utlSettings.component.html'
})
export class UtlSettingsComponent {

    theIp: string = '';
    thePort: string = '';

    showSave: boolean = true;
    showFetch: boolean = false;

    @Input()
    showMe: boolean = false;

    @Output()
    isVisibleEvent: EventEmitter<boolean> = new EventEmitter<boolean>();

    constructor(private utlsFileService: UtlsFileService, private zone: NgZone) {
        let utlIp = localStorage.getItem(AppConstants.UTL_SERVER_IP_KEY);
        if (utlIp) {
            this.theIp = utlIp;
        }
        else {
            this.theIp = AppConstants.UTL_SERVER_DEFAULT_IP;
        }
    }

    saveValues(): void {
        if (this.isSaveEnabled()) {
            localStorage.setItem(AppConstants.UTL_SERVER_IP_KEY, this.theIp);
            this.zone.run(() => {
                this.showSave = false;
                this.showFetch = true;
            });
        }
    }

    fetchLogs(): void {
        this.utlsFileService.fetchLogs();
        this.show(false);
    }

    close(): void {
        this.show(false);
    }

    private show(showMe: boolean): void {
        this.zone.run(() => {
            this.showMe = showMe;
            this.isVisibleEvent.emit(this.showMe);
        });
    }

    private isSaveEnabled(): boolean {
        return this.theIp && this.theIp.length > 0 ? true : false;
    }

}


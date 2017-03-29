import {Component, Output, EventEmitter, Input} from "@angular/core";
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

    constructor(private utlsFileService: UtlsFileService) {
        let utlIp = localStorage.getItem(AppConstants.UTL_SERVER_IP_KEY);
        if(utlIp){
            this.theIp = utlIp;
        }
        else{
            this.theIp = AppConstants.UTL_SERVER_DEFAULT_IP;
        }
    }

    saveValues(): void {
        if(this.isSaveEnabled()){
            localStorage.setItem(AppConstants.UTL_SERVER_IP_KEY, this.theIp);
            this.showSave = false;
            this.showFetch = true;
        }
    }

    fetchLogs(): void{
        this.utlsFileService.fetchLogs();
        this.showMe = false;
        this.isVisibleEvent.emit(this.showMe);
    }

    private isSaveEnabled(): boolean {
        return this.theIp && this.theIp.length > 0 ? true : false;
    }

}


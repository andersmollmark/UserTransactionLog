import {Component, OnInit} from "@angular/core";
import {UtlsFileService} from "./utls-file.service";
import {remote, ipcRenderer} from "electron";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Dto} from "./dto";

let {dialog} = remote;


@Component({
    selector: 'my-app',
    templateUrl: './app/app.component.html'
})
export class AppComponent implements OnInit {
    logs$: Observable<UtlsLog[]>;
    public filterQuery = "";
    clock;
    public isLoaded: boolean;
    public sortBy = "timestampAsDate";
    public sortOrder = "asc";

    //Select-column-filter
    selectedColumnDefaultChoice = "--- Select column ---";
    public selectedColumn = this.selectedColumnDefaultChoice;
    allColumns = "All";
    cols = [
        {name: "Username", value: UtlsFileService.USERNAME},
        {name: "Active tab", value: UtlsFileService.TAB},
        {name: "Category", value: UtlsFileService.CATEGORY},
        {name: "Eventname", value: UtlsFileService.EVENTNAME}
    ];

    //Selected-column-filter
    selectedContentDefaultChoice = "--- Select ---";
    public selectedContent = this.selectedContentDefaultChoice;
    allContent = "All";
    columnContent: Dto[];


    constructor(private utlsFileService: UtlsFileService) {
        this.isLoaded = false;
    }

    ngOnInit(): void {
        let self = this;
        var menu = remote.Menu.buildFromTemplate([{
            label: 'Menu',
            submenu: [
                {
                    label: 'open logfile',
                    click: function () {
                        dialog.showOpenDialog(self.handleFile);
                    }
                }
            ]
        }]);
        remote.Menu.setApplicationMenu(menu);

        this.clock = Observable.interval(1000);

    }

    public handleFile = (fileNamesArr: Array<any>) => {
        if (!fileNamesArr) {
            console.log("No file selected");
        }
        else {
            console.log("filename selected:" + fileNamesArr[0]);
            this.selectedColumn = this.selectedColumnDefaultChoice;
            this.selectedContent = this.selectedContentDefaultChoice;
            this.logs$ = this.utlsFileService.createLogs(fileNamesArr[0]);
            this.isLoaded = true;
        }
    }

    changeColumn(newColumn) {
        this.selectedColumn = newColumn;
        if (this.allColumns !== newColumn && this.selectedColumnDefaultChoice !== newColumn) {
            this.columnContent = this.utlsFileService.getContentForSpecificColumn(newColumn);
            this.selectedContent = this.selectedContentDefaultChoice;
        }
        if (this.allColumns === newColumn) {
            let emptyContent = new Array<Dto>();
            this.columnContent = emptyContent;
            this.logs$ = this.utlsFileService.getAllLogs();
        }
    }

    changeLogContent(newValueFromSpecificColumn) {
        if (this.allContent !== newValueFromSpecificColumn && this.selectedContentDefaultChoice !== newValueFromSpecificColumn) {
            this.logs$ = this.utlsFileService.getLogsForSpecificColumnValue(newValueFromSpecificColumn);
        }
        if (this.allContent === newValueFromSpecificColumn) {
            this.logs$ = this.utlsFileService.getAllLogs();
        }
    }


    ngOnDestroy() {
    }

}
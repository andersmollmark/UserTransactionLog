import {Injectable, NgZone} from "@angular/core";
import {UtlsLog} from "./utls-log";
import {Observable} from "rxjs/Observable";
import {Http} from "@angular/http";
import {Dto} from "./dto";
import {AppConstants} from "./app.constants";
import {UtlserverService} from "./utlserver.service";
import {Result} from "./result";
import {Subject} from "rxjs/Subject";
import {FetchLogParam} from "./fetchLogParam";
import {CryptoService} from "./crypto.service";
import {LogMessage} from "./logMessage";
import {Subscriber} from "rxjs/Subscriber";
import {FetchLogResult} from "./fetchLogResult";
import {FilterResult} from "./filter-result";
import {TimeFilterService} from "./timefilter.service";
import {Subscription} from "rxjs";
import moment = require("moment");

let fileSystem = require('fs');
const electron = require('electron');
const path = require('path');


@Injectable()
export class UtlsFileService {

    activeLogContent: Observable<UtlsLog[]>;

    activeTimezoneId: string = '';

    columnContentHasChanged: boolean = false;
    private openLogsWhenFileIsFetched: boolean = false;

    usersInLogContent: Dto[] = [];
    categoriesInLogContent: Dto[] = [];
    tabsInLogContent: Dto[] = [];
    eventNamesInLogContent: Dto[] = [];
    allColumnContent = [];

    websocketObservable: Subject<any>;

    logsResultSubject: Subject<UtlsLog[]> = new Subject<UtlsLog[]>();

    originFromFile = {
        usersInLogContent: [],
        categoriesInLogContent: [],
        tabsInLogContent: [],
        eventNamesInLogContent: [],
        allColumnContent: []
    };

    allLogsFromFile: UtlsLog[] = [];

    constructor(private http: Http, private utlsserverService: UtlserverService, private cryptoService: CryptoService,
                private timeFilterService: TimeFilterService, private zone: NgZone) {
    }

    ngOnDestroy() {
        console.log('destroying and unsubscribing');
        if (this.websocketObservable) {
            this.websocketObservable.unsubscribe();
        }
    }

    setOpenWhenFileIsFetched(open: boolean) {
        this.openLogsWhenFileIsFetched = open;
    }

    isOpenWhenFileIsFetched(): boolean {
        return this.openLogsWhenFileIsFetched;
    }

    createLogsFromFile(filename: string): void {
        this.init();
        this.zone.run(() => {
            this.getLogsFromFile(filename).subscribe(logs => {
                    this.logsResultSubject.next(logs);
                },
                error => {
                    let errMsg = 'Something went wrong while reading file:' + filename + ', error:' + error;
                    alert(errMsg);
                    console.log(errMsg);
                    this.logsResultSubject.error(error);
                });
        });
    }

    subscribeOnLogChanges(): Subject<UtlsLog[]> {
        this.logsResultSubject.complete();
        this.logsResultSubject = new Subject<UtlsLog[]>();
        return this.logsResultSubject;
    }

    private getLogsFromFile(filename: string): Observable<UtlsLog[]> {
        this.activeLogContent = null;
        this.zone.run(() => {
            this.activeLogContent = this.http.get(filename).map(res => {
                let content: LogMessage = LogMessage.fromResponse(res);
                let logs: UtlsLog[] = content.logs;
                this.activeTimezoneId = content.timezoneId;
                console.log('file read');
                if (content.is(AppConstants.UTL_LOGS_LAST_DAY) || content.is(AppConstants.UTL_LOGS_BACKUP_FETCH_LOGS)) {
                    console.log('yep, and it was a encrypted file');
                    logs = this.getDecryptedLogs(content);
                }
                else if (logs === null) {
                    logs = this.createUtlLogs(content.jsondump);
                }
                this.mapLogToContentAndColumn(logs);
                this.allLogsFromFile = logs;
                return logs;
            })
                .catch(error => Observable.throw(error.json ? error.json().error : alert("Error when reading file:" + filename + "," + error) || 'Server error'));
        });
        return this.activeLogContent;
    }

    getDecryptedLogs(logMessage: LogMessage): UtlsLog[] {
        let decryptedContent = this.cryptoService.doDecryptContent(logMessage.jsondump);
        return this.createUtlLogs(decryptedContent);
    }

    private createUtlLogs(logsString: string) {
        let result: UtlsLog[] = [];
        let logs: UtlsLog[] = JSON.parse(logsString);
        logs.forEach(log => {
            result.push(Object.assign(new UtlsLog(), log));
        });
        return result;
    }


    fetchLogs(fetchLogParam: FetchLogParam): Observable<Result> {
        console.log('utls-file-service and fetching logs...');
        let result = new Observable(observer => {
            this.init();
            this.utlsserverService.connectAndFetchEncryptedDump(fetchLogParam);
            this.websocketObservable = this.utlsserverService.utlServerWebsocket;
            let socketSubscription = this.websocketObservable.subscribe(fetchLogResult => {
                    if (!fetchLogResult) {
                        console.log('no dump received in utls-file-service');
                        observer.next(new Result('No logs is received', false));
                    }
                    else {
                        this.handleDump(fetchLogResult, observer);
                    }
                    socketSubscription.unsubscribe();
                },
                error => {
                    console.log('***** error on websocket:' + error);
                    socketSubscription.unsubscribe();
                    observer.error(new Error('Error on websocket:' + error));
                },
                () => {
                    console.log('***** websocket completed:');
                    socketSubscription.unsubscribe();
                    observer.complete();
                }
            );

        });
        return result;
    }

    private handleDump(fetchLogResult: FetchLogResult, observer: Subscriber<Result>) {
        let jsondata = JSON.parse(fetchLogResult.jsondump);
        if (jsondata.length === 0) {
            console.log('dump is empty in utls-file-service');
            observer.next(new Result('There is no logs in selected timespan', false));
        }
        else {
            console.log('dump received in utls-file-service');
            let prettyPrint = JSON.stringify(fetchLogResult, null, '\t');
            let fileSuffix = moment().format('YYYY_MM_DD_HHmmss').concat(AppConstants.UTL_JSON_FILE_SUFFIX);
            let filename = 'dump' + fileSuffix;
            let fileAndPath = path.join(electron.remote.app.getAppPath() + '/' + filename);
            console.log('writing file:' + filename + ' and hole path:' + fileAndPath);
            fileSystem.writeFile(fileAndPath, prettyPrint, (err) => {
                if (err) {
                    observer.next(new Result('something went wrong:' + err, false));
                    throw err;
                }
                console.log('file ' + filename + ' is saved');
                observer.next(new Result(filename, true));
            });
        }
    }


    private init() {
        if (this.usersInLogContent && this.usersInLogContent.length > 0) {
            this.usersInLogContent = [];
            this.categoriesInLogContent = [];
            this.tabsInLogContent = [];
            this.eventNamesInLogContent = [];
            this.allLogsFromFile = [];
            this.activeLogContent = Observable.of([]);
        }
    }


    createColumnFilteringValuesForLogs(logarray: UtlsLog[]) {
        this.createLogContentAndColumn(logarray);
        this.logsResultSubject.next(logarray);
    }

    private mapLogToContentAndColumn(logs: UtlsLog[]) {
        if (logs.length > 0) {
            this.createLogContentAndColumn(logs);
            this.setOriginalStructureFromFile();
        }
        else {
            alert('File contains no logs');
        }

    }

    setOriginalStructureFromFile() {
        this.originFromFile.allColumnContent[AppConstants.COL_USERNAME] = this.allColumnContent[AppConstants.COL_USERNAME];
        this.originFromFile.allColumnContent[AppConstants.COL_TAB] = this.allColumnContent[AppConstants.COL_TAB];
        this.originFromFile.allColumnContent[AppConstants.COL_CATEGORY] = this.allColumnContent[AppConstants.COL_CATEGORY];
        this.originFromFile.allColumnContent[AppConstants.COL_EVENTNAME] = this.allColumnContent[AppConstants.COL_EVENTNAME];
    }

    private createLogContentAndColumn(logs: UtlsLog[]): void {
        this.resetColumnData();
        let filterResult = new FilterResult();
        logs.forEach(log => {
            this.createPossibleColumnFilter(log, filterResult);
        });

        this.addColumndataToAllColumns();
    }

    private resetColumnData() {
        this.usersInLogContent = [];
        this.categoriesInLogContent = [];
        this.tabsInLogContent = [];
        this.eventNamesInLogContent = [];
        this.allColumnContent = [];

    }

    private createPossibleColumnFilter(log: UtlsLog, filterResult: FilterResult) {
        if (filterResult.tempUser.indexOf(log.username) === -1) {
            filterResult.tempUser.push(log.username);
            this.usersInLogContent.push(new Dto(AppConstants.COL_USERNAME, log.username));
        }
        if (filterResult.tempTab.indexOf(log.tab) === -1) {
            filterResult.tempTab.push(log.tab);
            this.tabsInLogContent.push(new Dto(AppConstants.COL_TAB, log.tab));
        }
        if (filterResult.tempCategory.indexOf(log.category) === -1) {
            filterResult.tempCategory.push(log.category);
            this.categoriesInLogContent.push(new Dto(AppConstants.COL_CATEGORY, log.category));
        }
        if (filterResult.tempName.indexOf(log.name) === -1) {
            filterResult.tempName.push(log.name);
            this.eventNamesInLogContent.push(new Dto(AppConstants.COL_EVENTNAME, log.name));
        }
    }

    private addColumndataToAllColumns() {
        this.allColumnContent[AppConstants.COL_USERNAME] = this.usersInLogContent;
        this.allColumnContent[AppConstants.COL_TAB] = this.tabsInLogContent;
        this.allColumnContent[AppConstants.COL_CATEGORY] = this.categoriesInLogContent;
        this.allColumnContent[AppConstants.COL_EVENTNAME] = this.eventNamesInLogContent;
        this.columnContentHasChanged = true;
    }

    isColumnContentChanged() {
        return this.columnContentHasChanged;
    }

    getContentForSpecificColumn(column: Dto): Dto[] {
        this.columnContentHasChanged = false;
        return this.allColumnContent[column.value];

    }


    getAllLogs(): void {
        this.mapLogToContentAndColumn(this.allLogsFromFile);
        this.logsResultSubject.next(this.allLogsFromFile);
    }

    getActiveTimezoneId(): string {
        return this.activeTimezoneId ? this.activeTimezoneId : null;
    }


}
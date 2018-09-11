import {Injectable, NgZone} from "@angular/core";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Http} from "@angular/http";
import {Dto} from "./dto";
import * as _ from "lodash";
import {AppConstants} from "./app.constants";
import {UtlserverService} from "./utlserver.service";
import {Result} from "./result";
import {Subject} from "rxjs/Subject";
import {FetchLogParam} from "./fetchLogParam";
import {CryptoService} from "./crypto.service";
import {LogMessage} from "./logMessage";
import {Subscriber} from "rxjs/Subscriber";
import moment = require("moment");

let fileSystem = require('fs');
const electron = require('electron');
const path = require('path');


@Injectable()
export class UtlsFileService {

    activeLogContent: Observable<UtlsLog[]>;
    partOfLogContent: Observable<UtlsLog[]>;

    columnContentHasChanged: boolean = false;
    private openLogsWhenFileIsFetched: boolean = false;

    usersInLogContent: Dto[] = [];
    categoriesInLogContent: Dto[] = [];
    tabsInLogContent: Dto[] = [];
    eventNamesInLogContent: Dto[] = [];
    allColumnContent = [];

    websocketObservable: Subject<any>;

    originFromFile = {
        usersInLogContent: [],
        categoriesInLogContent: [],
        tabsInLogContent: [],
        eventNamesInLogContent: [],
        allColumnContent: []
    };

    constructor(private http: Http, private utlsserverService: UtlserverService, private cryptoService: CryptoService, private zone: NgZone) {
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

    createLogsFromFile(filename: string): Subject<UtlsLog[]> {
        this.init();
        let resultSubject = new Subject();
        this.zone.run(() => {
            this.getLogsFromFile(filename).subscribe(logs => {
                    resultSubject.next(logs);
                },
                error => {
                    console.log('Something went wrong while reading file:' + filename + ', error:' + error);
                    resultSubject.error(error);
                });
        });
        return resultSubject;
    }

    private getLogsFromFile(filename: string): Observable<UtlsLog[]> {
        this.activeLogContent = null;
        this.zone.run(() => {
            this.activeLogContent = this.http.get(filename).map(res => {
                // let jsonstring = JSON.stringify(res.json());
                let content: LogMessage = LogMessage.fromResponse(res);
                // let content:LogMessage = JsonConverter.deserializeObject(res, TestMessage);
                let logs: UtlsLog[] = content.logs;
                console.log('file read');
                if (content.is(AppConstants.UTL_LOGS_LAST_DAY) || content.is(AppConstants.UTL_LOGS_BACKUP_FETCH_LOGS)) {
                    console.log('yep, and it was a encrypted file');
                    logs = this.getDecryptedLogs(content);
                }
                this.mapLogToContentAndColumn(logs);
                return logs;
            })
                .catch(error => Observable.throw(error.json ? error.json().error : alert("Error when reading file:" + filename + "," + error) || 'Server error'));
        });
        return this.activeLogContent;
    }

    getDecryptedLogs(logMessage: LogMessage): UtlsLog[] {
        let decryptedContent = this.cryptoService.doDecryptContent(logMessage.jsondump);
        let result: UtlsLog[] = [];
        let logs: UtlsLog[] = JSON.parse(decryptedContent);
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
            let socketSubscription = this.websocketObservable.subscribe(dump => {
                    if (!dump) {
                        console.log('no dump received in utls-file-service');
                        observer.next(new Result('No logs is received', false));
                    }
                    else {
                        this.handleDump(dump, observer);
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

    private handleDump(dump: any, observer: Subscriber<Result>) {
        let jsondata = JSON.parse(dump);
        if (jsondata.length === 0) {
            console.log('dump is empty in utls-file-service');
            observer.next(new Result('There is no logs in selected timespan', false));
        }
        else {
            console.log('dump received in utls-file-service');
            let prettyPrint = JSON.stringify(jsondata, null, '\t');
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
            this.activeLogContent = Observable.of([]);
        }
    }


    createColumnFilteringValuesForLogs(logarray: any[]) {
        this.createLogContentAndColumn(logarray);
    }

    mapLogToContentAndColumn(logs: UtlsLog[]) {
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

    resetContentAndColumnToOriginFromFile() {
        this.usersInLogContent = this.originFromFile.allColumnContent[AppConstants.COL_USERNAME];
        this.tabsInLogContent = this.originFromFile.allColumnContent[AppConstants.COL_TAB];
        this.categoriesInLogContent = this.originFromFile.allColumnContent[AppConstants.COL_CATEGORY];
        this.eventNamesInLogContent = this.originFromFile.allColumnContent[AppConstants.COL_EVENTNAME];
        this.addColumndataToAllColumns();
    }

    private createLogContentAndColumn(logs: any[]): void {
        let tempStructure = {
            tempUser: [],
            tempTab: [],
            tempCategory: [],
            tempName: []
        };
        let self = this;
        this.resetColumnData();
        _.forEach(logs, function (log) {
            self.createPossibleColumnFilter(self, log, tempStructure);
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

    private createPossibleColumnFilter(self, log, tempStructure){
        if (tempStructure.tempUser.indexOf(log.username) === -1) {
            tempStructure.tempUser.push(log.username);
            self.usersInLogContent.push({name: AppConstants.COL_USERNAME, value: log.username});
        }
        if (tempStructure.tempTab.indexOf(log.tab) === -1) {
            tempStructure.tempTab.push(log.tab);
            self.tabsInLogContent.push({name: AppConstants.COL_TAB, value: log.tab});
        }
        if (tempStructure.tempCategory.indexOf(log.category) === -1) {
            tempStructure.tempCategory.push(log.category);
            self.categoriesInLogContent.push({name: AppConstants.COL_CATEGORY, value: log.category});
        }
        if (tempStructure.tempName.indexOf(log.name) === -1) {
            tempStructure.tempName.push(log.name);
            self.eventNamesInLogContent.push({name: AppConstants.COL_EVENTNAME, value: log.name});
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


    getLogsForSpecificColumnValue(content: Dto): Observable<UtlsLog[]> {
        this.partOfLogContent =
            this.activeLogContent.map(logs => logs.filter(log => {
                if (log[content.name] === content.value) {
                    return true;
                }
                return false;
            }));
        return this.partOfLogContent;

    }

    getAllLogs(): Observable<UtlsLog[]> {
        return this.activeLogContent;
    }


}
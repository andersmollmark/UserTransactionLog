import {Injectable} from "@angular/core";
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
import moment = require("moment");

let fileSystem = require('fs');

@Injectable()
export class UtlsFileService {

    activeLogContent: Observable<UtlsLog[]>;
    partOfLogContent: Observable<UtlsLog[]>;

    encryptedFileContent: Observable<LogMessage>;
    public filereaderSubject: Subject<any>;


    columnContentHasChanged: boolean = false;
    private logfileIsFetched: boolean = false;
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

    constructor(private http: Http, private utlsserverService: UtlserverService, private cryptoService: CryptoService) {
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

    createLogs(filename: string): Observable<UtlsLog[]> {
        this.init();
        this.activeLogContent = this.http.get(filename).map(res => res.json())
            .catch(error => Observable.throw(error.json ? error.json().error : alert("Error when reading file:" + filename + "," + error) || 'Server error'));

        this.activeLogContent.subscribe(
            logs => this.mapLogToContentAndColumn(logs),
            error => console.log("something went wrong when mapping logs to columns"),
            () => console.log("done with mapping")
        );

        return this.activeLogContent;
    }

    createLogsFromEncryptedFile(filename: string): Observable<UtlsLog[]> {
        this.init();
        this.activeLogContent = this.http.get(filename).map(res => {
            let content = res.json();
            if (AppConstants.UTL_LOGS_LAST_DAY === content.messType) {
                console.log('yep, file read');
                return this.fixEncryptedLogs(content);
            }
            else {
                console.log('bummer, no file read, jsonmess:' + content);
                return [];
            }
        })
            .catch(error => Observable.throw(error.json ? error.json().error : alert("Error when reading file:" + filename + "," + error) || 'Server error'));
        return this.activeLogContent;
    }

    fixEncryptedLogs(logMessage: LogMessage): UtlsLog[] {
        let decryptedContent = this.cryptoService.doDecryptContent(logMessage.jsondump);
        let decryptedLogs = <UtlsLog[]> JSON.parse(decryptedContent);
        this.mapLogToContentAndColumn(decryptedLogs);
        return decryptedLogs;
    }


    fetchLogs(fetchLogParam: FetchLogParam): Observable<Result> {
        console.log('utls-file-service and fetching logs...');
        let result = new Observable(observer => {
            this.init();
            this.utlsserverService.connectAndFetchEncryptedDump(fetchLogParam);
            this.websocketObservable = this.utlsserverService.utlServerWebsocket;
            let socketSubscription = this.websocketObservable.subscribe(dump => {
                    console.log('dump received in utls-file-service');
                    if (dump) {
                        let jsondata = JSON.parse(dump);
                        let prettyPrint = JSON.stringify(jsondata, null, '\t');
                        let fileSuffix = moment().format('YYYY_MM_DD_HHmmss').concat(AppConstants.UTL_FILE_SUFFIX);
                        let filename = 'dump' + fileSuffix;
                        console.log('writing file:' + filename);
                        fileSystem.writeFile(filename, prettyPrint, (err) => {
                            if (err) {
                                observer.next(new Result('something went wrong:' + err, false));
                                throw err;
                            }
                            console.log('file ' + filename + ' is saved');
                            observer.next(new Result(filename, true));
                        });

                    }
                    else {
                        observer.next(new Result('no dump is received', false));
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
        this.createLogContentAndColumn(logs);
        this.setOriginalStructureFromFile();

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

    private createLogContentAndColumn(logs: any[]) {
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

    private createPossibleColumnFilter(self, log, tempStructure) {
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
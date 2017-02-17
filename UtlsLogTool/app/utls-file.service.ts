import {Injectable} from "@angular/core";
import {UtlsLog} from "./log";
import {Observable} from "rxjs/Observable";
import {Http} from "@angular/http";
import {Dto} from "./dto";
import * as _ from "lodash";

@Injectable()
export class UtlsFileService {

    activeLogContent: Observable<UtlsLog[]>;
    partOfLogContent: Observable<UtlsLog[]>;

    usersInLogContent: Dto[] = [];
    categoriesInLogContent: Dto[] = [];
    tabsInLogContent: Dto[] = [];
    eventNamesInLogContent: Dto[] = [];

    public static USERNAME = "username";
    public static TAB = "tab";
    public static CATEGORY = "category";
    public static EVENTNAME = "name";

    allColumnContent = [];

    constructor(private http: Http) {
    }

    createLogs(filename: string): Observable<UtlsLog[]> {
        this.init();
        this.activeLogContent =
            this.http.get(filename).map(res => res.json())
                .catch(error => Observable.throw(error.json ? error.json().error : alert("Error when reading file:" + filename + "," + error) || 'Server error'));

        this.activeLogContent.subscribe(
            logs => this.mapLogToContentAndColumn(logs),
            error => console.log("something went wrong when mapping logs to columns"),
            () => console.log("done with mapping")
        );

        return this.activeLogContent;
    }

    private init(){
        if(this.usersInLogContent && this.usersInLogContent.length > 0){
            this.usersInLogContent = [];
            this.categoriesInLogContent = [];
            this.tabsInLogContent = [];
            this.eventNamesInLogContent = [];
            this.activeLogContent = Observable.of([]);
        }
    }

    mapLogToContentAndColumn(logs: UtlsLog[]){
        let tempUser = [];
        let tempTab = [];
        let tempCategory = [];
        let tempName = [];
        let self = this;
        _.forEach(logs, function (log) {
            if(tempUser.indexOf(log.username) === -1){
                tempUser.push(log.username);
                self.usersInLogContent.push({name: UtlsFileService.USERNAME, value:log.username});
            }
            if(tempTab.indexOf(log.tab) === -1){
                tempTab.push(log.tab);
                self.tabsInLogContent.push({name: UtlsFileService.TAB, value:log.tab});
            }
            if(tempCategory.indexOf(log.category) === -1){
                tempCategory.push(log.category);
                self.categoriesInLogContent.push({name: UtlsFileService.CATEGORY, value:log.category});
            }
            if(tempName.indexOf(log.name) === -1){
                tempName.push(log.name);
                self.eventNamesInLogContent.push({name: UtlsFileService.EVENTNAME, value:log.name});
            }
        });

        this.allColumnContent[UtlsFileService.USERNAME] = this.usersInLogContent;
        this.allColumnContent[UtlsFileService.TAB] = this.tabsInLogContent;
        this.allColumnContent[UtlsFileService.CATEGORY] = this.categoriesInLogContent;
        this.allColumnContent[UtlsFileService.EVENTNAME] = this.eventNamesInLogContent;
    }


    getContentForSpecificColumn(column: Dto): Dto[]{
        return this.allColumnContent[column.value];

    }

    getLogsForSpecificColumnValue(content: Dto): Observable<UtlsLog[]>{
        this.partOfLogContent =
            this.activeLogContent.map(logs => logs.filter(log => {
                // console.log('value:' + log[content.name]);
                if(log[content.name] === content.value){
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
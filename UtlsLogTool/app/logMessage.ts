import {UtlsLog} from "./log";
import {Response} from "@angular/http";
import {AppConstants} from "./app.constants";

export class LogMessage {

    public jsondump: string;
    public messType: string;
    public logs: UtlsLog[];

    is(type: string): boolean {
        return this.messType === type;
    }


    static fromResponse(response: Response): LogMessage {
        let logMessage = new LogMessage();
        const jsonObj = response.json();
        if (Array.isArray(jsonObj)) {
            let result: UtlsLog[] = [];
            (<Object[]>jsonObj).forEach(log => {
                result.push(Object.assign(new UtlsLog(), log));
            });
            logMessage.logs = result;
            logMessage.messType = AppConstants.UTL_DUMP_MESSTYPE;
        }
        else {
            logMessage.jsondump = jsonObj.jsondump;
            logMessage.messType = jsonObj.messType;
        }
        return logMessage;
    }

}

import {UtlsLog} from "./utls-log";

export interface LogAction {
    execute(logs: UtlsLog[]): void;

}
import {UtlsLog} from "./log";

export interface LogAction {
    execute(logs: UtlsLog[]): void;

}
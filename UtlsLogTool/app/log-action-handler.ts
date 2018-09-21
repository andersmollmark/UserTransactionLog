import {LogAction} from "./log-action";

export class LogActionHandler {

    private static INSTANCE: LogActionHandler = null;
    private nextAction: LogAction = null;

    private constructor() {

    }

    static getInstance(): LogActionHandler {
        if(LogActionHandler.INSTANCE === null) {
            LogActionHandler.INSTANCE = new LogActionHandler();
        }
        return LogActionHandler.INSTANCE;
    }

    hasNext(): boolean {
        return this.nextAction !== null;
    }

    setNext(logAction: LogAction) {
        this.nextAction = logAction;
    }

    next(): LogAction {
        let result = this.nextAction;
        this.nextAction = null;
        return result;
    }
}
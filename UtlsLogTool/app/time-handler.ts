import {UtlsLog} from "./log";
import {InitTimezoneResult} from "./init-timezone-result";
import {CreateDateResult} from "./create-date-result";

export class TimeHandler {
    private momentTz = require("moment-timezone");
    private moment = require("moment");

    private static HR_MODE_12 = 'MMMM D, YYYY hh:mm:ss A';
    private static HR_MODE_24 = 'MMMM D, YYYY HH:mm:ss';

    private static INSTANCE: TimeHandler = null;
    private hourMode12: boolean = false;

    private constructor() {

    }

    static getInstance(): TimeHandler {
        if (TimeHandler.INSTANCE === null) {
            TimeHandler.INSTANCE = new TimeHandler();
        }
        return TimeHandler.INSTANCE;
    }

    set12HourMode(is12HourMode: boolean) {
        this.hourMode12 = is12HourMode;
    }

    initTimezonesAndGetEndDates(newTimezone: string, logs: UtlsLog[]): InitTimezoneResult {
        let result = new InitTimezoneResult();
        let format: string = this.getHourModeFormat();
        logs.forEach(log => {
            let dateAndMoment = this.createNewDateForTimezone(log.timestamp, newTimezone);
            this.fixTimezoneForLog(log, format, newTimezone, dateAndMoment);

            if (result.firstTimestamp === 0 || result.firstTimestamp > dateAndMoment.newDate.getTime()) {
                result.firstTimestamp = dateAndMoment.newDate.getTime();
            }
            else if (result.lastTimestamp === 0 || result.lastTimestamp < dateAndMoment.newDate.getTime()) {
                result.lastTimestamp = dateAndMoment.newDate.getTime();
            }
        });

        return result;
    }

    changeTimezone(newTimezone: string, logs: UtlsLog[]): void {
        let format: string = this.getHourModeFormat();
        logs.forEach(log => {
            let dateAndMoment = this.createNewDateForTimezone(log.timestamp, newTimezone);
            this.fixTimezoneForLog(log, format, newTimezone, dateAndMoment);
        });

    }

    private fixTimezoneForLog(log: UtlsLog, format: string, newTimezone: string, dateAndMoment: CreateDateResult): void {
        log.timestampAsDateString = dateAndMoment.newMoment.format(format);

        let timestampForNewTimezone = log.timestampWithTimezone.get(newTimezone);
        if (!timestampForNewTimezone || timestampForNewTimezone === null) {
            log.timestampWithTimezone.set(newTimezone, dateAndMoment.newDate.getTime());
        }
    }

    createNewDateForTimezone(timestamp: number, newTimezone: string): CreateDateResult {
        let result = new CreateDateResult();
        let logDate = new Date(timestamp);
        let newDate = this.getChangedDateDueToTimezone(logDate, newTimezone, logDate.getSeconds());
        let newMoment = this.moment(newDate);
        result.newDate = logDate;
        result.newMoment = newMoment;
        return result;
    }

    getChangedDateDueToTimezone(dateToChange: Date, timezoneId: string, seconds: number): Date {
        let valueMoment = this.momentTz(dateToChange);
        let utcMoment = valueMoment.utc();
        let newMoment = utcMoment.clone().tz(timezoneId);

        let changedDate = new Date(newMoment.year(), newMoment.month(), newMoment.date(), newMoment.hours(), newMoment.minutes(), seconds, 0);
        return changedDate;
    }

    changeHourMode(timezone: string, logs: UtlsLog[]): void {
        let format: string = this.getHourModeFormat();
        logs.forEach(log => {
            let timestampForTimezone = log.timestampWithTimezone.get(timezone);
            let date = this.moment(timestampForTimezone);
            log.timestampAsDateString = date.format(format);

        });
    }

    is12HourMode(): boolean {
        return this.hourMode12;
    }

    private getHourModeFormat(): string {
        return this.hourMode12 ? TimeHandler.HR_MODE_12 : TimeHandler.HR_MODE_24;
    }

}
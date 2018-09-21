import {TimeHandler} from "./time-handler";

export class SelectedDate {

    private FORMAT_12_HOURS_MODE: string = 'MMM D YYYY, h:mm a';
    private FORMAT_24_HOURS_MODE: string = 'MMM D YYYY, HH:mm';

    private static moment = require("moment");

    private originalTimestamp: number = 0;
    private valueCopy: Date = null;

    private timeHandler = TimeHandler.getInstance();


    private constructor(private value: Date, private hour12Mode: boolean) {
        this.originalTimestamp = value.getTime();
    }

    public static createOrigin(val: Date, hour12Mode: boolean, timezoneId: string): SelectedDate {
        let result = new SelectedDate(val, hour12Mode);
        result.value = result.timeHandler.getChangedDateDueToTimezone(new Date(result.originalTimestamp), timezoneId, 0);
        result.valueCopy = new Date(result.value.getTime());

        return result;
    }

    changeDueToNewTimezone(hour12Mode: boolean, newTimezone: string): SelectedDate {
        this.value = this.timeHandler.getChangedDateDueToTimezone(new Date(this.originalTimestamp), newTimezone, 0);
        console.log('before change:' + this.valueCopy +  'after changing to new timezone:' + newTimezone + ':' + this.value);
        this.valueCopy = new Date(this.value);

        return this;
    }

    public getValue(): Date{
        return this.value;
    }

    getOriginTimestamp(): number {
        return this.originalTimestamp;
    }

    asString(): string {
        return SelectedDate.moment(this.value).format(this.getFormat());
    }

    isChangedDate(): boolean {
        return this.value.getFullYear() !== this.valueCopy.getFullYear() ||
            this.value.getMonth() !== this.valueCopy.getMonth() ||
            this.value.getDate() !== this.valueCopy.getDate();
    }

    isChangedTime(newTime: Date): boolean {
        return newTime.getHours() !== this.valueCopy.getHours() ||
            newTime.getMinutes() !== this.valueCopy.getMinutes();
    }

    setTime(anotherTime: Date, seconds: number): void{
        this.value.setHours(anotherTime.getHours());
        this.value.setMinutes(anotherTime.getMinutes());
        this.value.setSeconds(seconds);
        this.value.setMilliseconds(0);

        this.updateDates();

    }

    updateDates(): void {
        console.log('originalTimestamp before:' + this.originalTimestamp + ':' + new Date(this.originalTimestamp));
        this.originalTimestamp = this.originalTimestamp + this.getDiff();
        console.log('originalTimestamp after:' + this.originalTimestamp + ':' + new Date(this.originalTimestamp));
        this.valueCopy = new Date(this.value.getTime());
    }

    private getDiff(): number {
        return this.value.getTime() - this.valueCopy.getTime();
    }

    private getFormat(): string {
        return this.hour12Mode ? this.FORMAT_12_HOURS_MODE : this.FORMAT_24_HOURS_MODE;
    }

}

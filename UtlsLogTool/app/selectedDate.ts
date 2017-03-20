import moment = require("moment");
export class SelectedDate {

    public FORMAT_MMM_D_YYY_H_MM_A: string = 'MMM D YYYY, h:mm a';
    value: Date;

    private constructor(val: Date) {
        this.value = val;
    }

    public static getFromDateParts(val: Date): SelectedDate {
        let temp = new Date(val.getFullYear(), val.getMonth(), val.getDate());
        return new SelectedDate(temp);
    }

    public static getFromDate(val: Date): SelectedDate {
        return new SelectedDate(val);
    }

    asString(): string {
        return this.asFormatString(this.FORMAT_MMM_D_YYY_H_MM_A);
    }

    asFormatString(optionalFormat: string): string {
        return moment(this.value).format(optionalFormat ? optionalFormat : this.FORMAT_MMM_D_YYY_H_MM_A);
    }


    isSame(another: SelectedDate): boolean {
        return this.value && another && another.value &&
            this.value.getFullYear() === another.value.getFullYear() &&
            this.value.getMonth() === another.value.getMonth() &&
            this.value.getDate() === another.value.getDate();
    }

    isSameTime(anotherTime: Date): boolean {
        return this.value && anotherTime &&
            this.value.getHours() === anotherTime.getHours() &&
            this.value.getMinutes() === anotherTime.getMinutes();
    }

    setTime(anotherTime: Date, seconds: number): void{
        this.value.setHours(anotherTime.getHours());
        this.value.setMinutes(anotherTime.getMinutes());
        this.value.setSeconds(seconds);
    }

}

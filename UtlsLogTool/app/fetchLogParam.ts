import * as _ from "lodash";

export class FetchLogParam {

    from: Date;
    to: Date;
    timezone: string;

    isOk(): boolean{
        return !_.isNil(this.from) && !_.isNil(this.to) && !_.isNil(this.timezone);
    }

    getFrom(): Date{
        return this.from;
    }

    getTo(): Date{
        return this.to;
    }

    getTimezone(): string{
        return this.timezone;
    }

}

import * as _ from "lodash";

export class FetchLogParam {

    from: Date;
    to: Date;

    isOk(): boolean{
        return !_.isNil(this.from) && !_.isNil(this.to);
    }

    getFrom(): Date{
        return this.from;
    }

    getTo(): Date{
        return this.to;
    }

}

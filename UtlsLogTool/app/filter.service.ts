import {Injectable} from "@angular/core";

@Injectable()
export class FilterService {

    private timefilterFrom: Date;
    private timefilterTo: Date;
    private filterQuery: string = "";

    constructor() {
    }

    setTimefilterFrom(from: Date){
        this.timefilterFrom = from;
    }

    setTimefilterTo(to: Date){
        this.timefilterTo = to;
    }

    getTimefilterFrom(): Date{
        return this.timefilterFrom;
    }

    getTimefilterTo(): Date{
        return this.timefilterTo;
    }

    setFilterQuery(query: string){
        this.filterQuery = query;
    }


    getFilterQuery(): string{
        return this.filterQuery;
    }

}
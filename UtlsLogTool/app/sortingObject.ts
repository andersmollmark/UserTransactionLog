import {AppConstants} from "./app.constants";
export class SortingObject {
    sortorder: string = "";
    sortname: string = "";

    isOk(): boolean {
        return this.sortorder && this.sortorder.length > 0 &&
            this.sortname && this.sortname.length > 0;
    }

    isTimestamp(): boolean{
        return this.isOk() && this.sortname === AppConstants.COL_TIMESTAMP;
    }

}

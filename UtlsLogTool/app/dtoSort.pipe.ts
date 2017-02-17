import {Pipe, PipeTransform} from "@angular/core";
import {Dto} from "./dto";

@Pipe({
    name: "dtoSort"
})
export class DtoSortPipe implements PipeTransform {

    transform(array: Array<Dto>, args: string): Array<Dto> {
        if (array) {
            array.sort((a: any, b: any) => {
                if (a.name < b.name) {
                    return -1;
                } else if (a.name > b.name) {
                    return 1;
                } else {
                    return 0;
                }
            });
        }
        return array;
    }
}
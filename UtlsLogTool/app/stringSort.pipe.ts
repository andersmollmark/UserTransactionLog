import {Pipe, PipeTransform} from "@angular/core";

@Pipe({
    name: "stringSort"
})
export class StringSortPipe implements PipeTransform {

    transform(array: Array<string>, args: string): Array<string> {
        if (array) {
            array.sort((a: any, b: any) => {
                if (a < b) {
                    return -1;
                } else if (a > b) {
                    return 1;
                } else {
                    return 0;
                }
            });
        }
        return array;
    }
}
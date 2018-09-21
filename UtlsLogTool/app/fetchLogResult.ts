export class FetchLogResult {

    public jsondump: string;
    public timezoneId: string = null;

    public toString(): string {
        return JSON.stringify(this);
    }

}

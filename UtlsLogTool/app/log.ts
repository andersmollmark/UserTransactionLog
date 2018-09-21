
export class UtlsLog{
    id: string;
    username: string;
    name: string;
    category: string;
    label: string;
    userTransactionKeyId: string;
    timestamp: number;
    timestampAsDate: string;
    timestampAsDateString: string;
    tab: string;
    host: string;
    targetMs: string;
    target: string;
    timestampWithTimezone: Map<string, number> = new Map<string, number>();

}

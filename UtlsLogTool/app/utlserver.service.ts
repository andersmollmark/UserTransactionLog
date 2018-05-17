import {Injectable} from "@angular/core";
import {WebsocketService} from "./websocket.service";
import {Subject} from "rxjs";
import {AppConstants} from "./app.constants";
import {CryptoService} from "./crypto.service";
import {FetchLogParam} from "./fetchLogParam";


@Injectable()
export class UtlserverService {


    public utlServerWebsocket: Subject<any>;
    private utlURL: string = '';

    private ipAddr: string = '';
    private port: string = '';

    private from: Date;
    private to: Date;
    private chosenTimezone: string;

    constructor(private websocketService: WebsocketService, private cryptoService: CryptoService) {
        this.setIpAndPort();
    }

    connectAndFetchEncryptedDump(fetchLogParam: FetchLogParam): void {
        this.setIpAndPort();
        if (this.ipAddr && this.port && this.ipAddr.length > 0 && this.port.length > 0) {
            this.utlURL = AppConstants.UTL_SERVER_URL_PREFIX.concat(this.ipAddr).concat(':').concat(this.port).concat(AppConstants.UTL_SERVER_URL_SUFFIX);

            this.from = fetchLogParam.getFrom();
            this.to = fetchLogParam.getTo();
            this.chosenTimezone = fetchLogParam.getTimezone();
            this.utlServerWebsocket = <Subject<any>>this.websocketService
                .connect(this.utlURL, this.fetchDump.bind(this))
                .map((response: MessageEvent): any => {
                    if (response && response.data) {
                        let jsonMessage = JSON.parse(response.data);
                        if (AppConstants.UTL_LOG_DUMPMESSAGE === jsonMessage.messType) {
                            console.log('yep, dumpmessage arrived');
                            return this.cryptoService.doDecryptContent(jsonMessage.jsondump);
                        }
                        console.log('bummer, not dumpmessage arrived');
                    }
                    return undefined;
                });
        }
        else {
            alert('you have to set ip to utlserver');
        }
    }

    private setIpAndPort(): void {
        let ipAddr = localStorage.getItem(AppConstants.UTL_SERVER_IP_KEY);
        this.ipAddr = ipAddr ? ipAddr : AppConstants.UTL_SERVER_DEFAULT_IP;
        this.port = AppConstants.UTL_SERVER_PORT;
    }

    getUtlsIp(): string {
        this.setIpAndPort();
        return this.ipAddr;
    }

    private fetchDump(): void {
        let socketMessage = {
            fromInMillis: this.from.getTime(),
            toInMillis: this.to.getTime(),
            timezone: this.chosenTimezone,
            messType: AppConstants.UTL_LOG_DUMPMESSAGE,
        };

        this.utlServerWebsocket.next(socketMessage);

    }

}

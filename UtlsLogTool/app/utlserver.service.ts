import {Injectable} from "@angular/core";
import {WebsocketService} from "./websocket.service";
import * as Rx from 'rxjs/Rx';
import {Subject} from "rxjs";
import {Message} from "@angular/compiler/src/i18n/i18n_ast";
import {AppConstants} from "./app.constants";

export interface UtlsLogMessage {
    messType: string,
    jsondump: string
}

@Injectable()
export class UtlserverService {


    public utlServerWebsocket: Subject<any>;
    private utlURL: string = '';

    private ipAddr: string = '';
    private port: string = '';

    constructor(private websocketService: WebsocketService) {
        this.setIpAndPort();
    }

    connectAndFetchDump(): void {
        this.setIpAndPort();
        if(this.ipAddr && this.port && this.ipAddr.length > 0 && this.port.length > 0){
            this.utlURL = AppConstants.UTL_SERVER_URL_PREFIX.concat(this.ipAddr).concat(':').concat(this.port).concat(AppConstants.UTL_SERVER_URL_SUFFIX);

            this.utlServerWebsocket = <Subject<any>>this.websocketService
                .connect(this.utlURL, this.fetchDump.bind(this))
                .map((response: MessageEvent): any => {
                    if(response && response.data){
                        let data = JSON.parse(response.data);
                        if (AppConstants.UTL_LOG_DUMPMESSAGE === data.messType) {
                            console.log('yep, dumpmessage arrived');
                            return data.jsondump;
                        }
                        console.log('bummer, not dumpmessage arrived');
                    }
                    return undefined;
                });
        }
        else{
            alert('you have to set ip to utlserver');
        }
    }

    private setIpAndPort(): void {
        let ipAddr = localStorage.getItem(AppConstants.UTL_SERVER_IP_KEY);
        this.ipAddr = ipAddr ? ipAddr : AppConstants.UTL_SERVER_DEFAULT_IP;
        this.port = AppConstants.UTL_SERVER_PORT;
    }

    getUtlsIp(): string {
        return this.ipAddr;
    }

    private fetchDump(): void {
        let socketMessage = {
            jsonContent: '',
            client: '',
            username: '',
            messType: AppConstants.UTL_LOG_DUMPMESSAGE,
            target: ''
        };

        this.utlServerWebsocket.next(socketMessage);

    }

}


import {Injectable} from "@angular/core";
import * as Rx from 'rxjs/Rx';

@Injectable()
export class WebsocketService {

    constructor(){}

    private subject: Rx.Subject<MessageEvent>;

    public connect(url, onOpenCallback): Rx.Subject<MessageEvent> {
        if (!this.isSocketOpen()) {
            this.subject = this.create(url, onOpenCallback);
            console.log("Successfully connected: " + url);
        }
        else if(this.isSocketOpen() && onOpenCallback) {
            console.log('socket open and calling callback');
            onOpenCallback.call();
        }
        return this.subject;
    }

    private isSocketOpen(): boolean {
        return this.subject && !this.subject.closed && !this.subject.isStopped;
    }

    private create(url, onOpenCallback): Rx.Subject<MessageEvent> {
        let ws = new WebSocket(url);

        let observable = Rx.Observable.create(
            (obs: Rx.Observer<MessageEvent>) => {
                ws.onmessage = obs.next.bind(obs);
                ws.onerror = obs.error.bind(obs);
                ws.onclose = obs.complete.bind(obs);
                return ws.close.bind(ws);
            });
        let observer = {
            next: (data: Object) => {
                if (ws.readyState === WebSocket.OPEN) {
                    ws.send(JSON.stringify(data));
                }
            }
        };

        ws.onopen = function () {
            console.log('onopen...');
            if(onOpenCallback){
                console.log('calling callback');
                onOpenCallback.call();
            }
        };
        return Rx.Subject.create(observer, observable);
    }

}

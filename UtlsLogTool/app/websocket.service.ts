import {Injectable} from "@angular/core";
import * as Rx from 'rxjs/Rx';
import {Subject} from "rxjs/Subject";

@Injectable()
export class WebsocketService {

    constructor() {
    }

    private socket: Rx.Subject<MessageEvent>;
    public socketObservable: Subject<any>;
    private websocket: WebSocket;

    ngOnDestroy() {
        console.log('destroying and unsubscribing');
        if (this.socketObservable) {
            this.socketObservable.unsubscribe();
        }
    }

    public connect(url, onOpenCallback): Rx.Subject<MessageEvent> {
        this.socketObservable = <Subject<any>>this.connectAndGetSocket(url, onOpenCallback);

        this.socketObservable.subscribe(
            mess => console.log('WebsocketService, message received on websocket...'),
            error => {
                console.log('WebsocketService, error on websocket...:' + error);
                this.websocket.close();
                this.socket = null;
            },
            () => {
                console.log('WebsocketService completed on websocket... closing socket');
                this.websocket.close();
                this.socket = null;
            }
        );

        return this.socketObservable;
    }

    private connectAndGetSocket(url, onOpenCallback): Rx.Subject<MessageEvent> {
        if (!this.isSocketOpen()) {
            this.socket = this.create(url, onOpenCallback);
            console.log("Successfully connected: " + url);
        }
        else if (this.isSocketOpen() && onOpenCallback) {
            console.log('socket open and calling callback');
            onOpenCallback.call();
        }
        return this.socket;
    }

    private isSocketOpen(): boolean {
        return this.socket && !this.socket.closed && !this.socket.isStopped;
    }

    private create(url, onOpenCallback): Rx.Subject<MessageEvent> {
        this.websocket = new WebSocket(url);

        let observable = Rx.Observable.create(
            (obs: Rx.Observer<MessageEvent>) => {
                this.websocket.onmessage = obs.next.bind(obs);
                this.websocket.onerror = obs.error.bind(obs);
                this.websocket.onclose = obs.complete.bind(obs);
                return this.websocket.close.bind(this.websocket);
            }).share();

        let observer = {
            next: (data: Object) => {
                if (this.websocket.readyState === WebSocket.OPEN) {
                    this.websocket.send(JSON.stringify(data));
                }
            }
        };

        this.websocket.onopen = function () {
            console.log('onopen...');
            if (onOpenCallback) {
                console.log('calling callback');
                onOpenCallback.call();
            }
        };

        return Rx.Subject.create(observer, observable);
    }

}

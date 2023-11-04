import {Injectable, OnDestroy} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {JobChat, UpdateChatRequest} from "../model/chat";
import {RxStompService} from "./rx-stomp.service";
import {Message} from "@stomp/stompjs";
import {takeUntil} from "rxjs/operators";
import {RxStompConfig} from "@stomp/rx-stomp";
import {AuthenticationService} from "./authentication.service";

@Injectable({
  providedIn: 'root'
})
export class ChatService implements OnDestroy {

  private apiUrl: string = environment.apiUrl + '/chat';
  private stompServiceConfigured = false;
  private destroyStompSubscriptions$ = new Subject<void>();
  private observables: Map<number, Observable<Message>> = new Map<number, Observable<Message>>();

  constructor(
      private authenticationService: AuthenticationService,
      private http: HttpClient,
      private rxStompService: RxStompService
  ) {
  }

  ngOnDestroy(): void {
    this.disconnect();
  }

  create(request: UpdateChatRequest): Observable<JobChat> {
    return this.http.post<JobChat>(`${this.apiUrl}`, request);
  }

  list(): Observable<JobChat[]> {
    //If we already have the data return it, otherwise get it.
    return this.http.get<JobChat[]>(`${this.apiUrl}`)
  }

  subscribe(chat: JobChat): Observable<Message> {

    //Check if we already have an observable for this chat.
    let observable = this.observables.get(chat.id);
    if (observable == null) {

      //Not yet subscribed to this chat - subscribe and save the observable.
      this.configureStompService();

      observable = this.rxStompService.watch('/topic/chat/' + chat.id)
      //This pipe allows us to keep track of subscriptions so that we can unsubscribe on destroy
      //See https://www.learnrxjs.io/learn-rxjs/operators/filtering/takeuntil
      .pipe(takeUntil(this.destroyStompSubscriptions$));

      //Save observable for this chat.
      this.observables.set(chat.id, observable);
    }

    return observable;
  }

  disconnect() {
    this.unsubscribeAll();

    this.rxStompService.deactivate();
    this.stompServiceConfigured = false;
  }
  unsubscribeAll() {
    //Unsubscribe all stomp subscriptions
    //See https://www.learnrxjs.io/learn-rxjs/operators/filtering/takeuntil
    this.destroyStompSubscriptions$.next();
    this.destroyStompSubscriptions$.complete();
  }

  private configureStompService() {
    //Check if already configured
    if (!this.stompServiceConfigured) {
      let stompConfig = this.getRxStompConfig();
      this.rxStompService.configure(stompConfig);
      console.log(Date(), 'Connecting to ' + stompConfig.brokerURL)
      this.rxStompService.activate();
      this.stompServiceConfigured = true;
    }
  }

  /**
   * Returns an RxStompConfig, populated with the current Authorization header token in
   * currentHeaders.
   */
  private getRxStompConfig(): RxStompConfig {

    const protocol = environment.production ? 'wss' : 'ws';
    const config: RxStompConfig = {
      // Which server?
      //todo Not sure why need websocket on end but you do
      brokerURL: protocol + '://' + environment.host + '/jobchat/websocket',

      // Headers
      connectHeaders: {
      },

      // How often to heartbeat?
      // Interval in milliseconds, set to 0 to disable
      heartbeatIncoming: 0, // Typical value 0 - disabled
      heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

      // Wait in milliseconds before attempting auto reconnect
      // Set to 0 to disable
      // Typical value 500 (500 milli seconds)
      reconnectDelay: 5000,

      // Will log diagnostics on console
      // It can be quite verbose, not recommended in production
      // Skip this key to stop logging to console
      debug: (msg: string): void => {
        console.log(new Date(), msg);
      },
    }

    let host = document.location.host;

    const token = this.authenticationService.getToken();
    if (token) {
      config.connectHeaders.Authorization = `Bearer ${token}`
    }

    return config;
  }

}

import {Injectable, OnDestroy} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {merge, Observable, Subject, Subscription} from "rxjs";
import {CreateChatRequest, JobChat} from "../model/chat";
import {RxStompService} from "./rx-stomp.service";
import {Message} from "@stomp/stompjs";
import {map, takeUntil} from "rxjs/operators";
import {RxStompConfig} from "@stomp/rx-stomp";
import {AuthenticationService} from "./authentication.service";

@Injectable({
  providedIn: 'root'
})
export class ChatService implements OnDestroy {

  private apiUrl: string = environment.chatApiUrl + '/chat';
  private stompServiceConfigured = false;

  /**
   * All Observables coming from Stomp watches are piped with a takeUntil this subject.
   * This allows us to unsubscribe from all of them by means of this subject.
   * See unsubscribeAll below.
   */
  private destroyStompSubscriptions$ = new Subject<void>();

  /**
   * Map of Chat id to Observable for that chat
   */
  private observables: Map<number, Observable<Message>> = new Map<number, Observable<Message>>();

  private authenticationServiceSubscription: Subscription = null;

  //todo Should look this up from Map by chat id like observables
  userMarkedChatAsRead$ = new Subject<void>();

  constructor(
      private authenticationService: AuthenticationService,
      private http: HttpClient,
      private rxStompService: RxStompService
  ) {

    //Subscribe to authentication service so that we can detect logouts and disconnect on a logout.
    this.authenticationServiceSubscription = this.authenticationService.loggedInUser$.subscribe(
      (user) => {
        if (user == null) {
          //Disconnect chat on logout - ie when loggedInUser becomes null.
          this.disconnect();
        }
      }
    )
  }

  ngOnDestroy(): void {
    this.disconnect();
    if (this.authenticationServiceSubscription) {
      this.authenticationServiceSubscription.unsubscribe();
    }
  }

  create(request: CreateChatRequest): Observable<JobChat> {
    return this.http.post<JobChat>(`${this.apiUrl}`, request);
  }

  getOrCreate(request: CreateChatRequest): Observable<JobChat> {
    return this.http.post<JobChat>(`${this.apiUrl}/get-or-create`, request)
  }

  list(): Observable<JobChat[]> {
    //If we already have the data return it, otherwise get it.
    return this.http.get<JobChat[]>(`${this.apiUrl}`)
  }

  subscribeChatReadStatus(chat: JobChat): Observable<boolean> {
    //New post events coming from server
    let newPosts$ = this.subscribe(chat).pipe(
      //New posts set the chat read status to false
      map(message => false),
    )

    const userMarkedChatAsRead$ = this.getMarkedChatAsReadSubject(chat);
    //Events signalling that user has read the chat
    let markedAsRead$ = userMarkedChatAsRead$.pipe(
      //Read events come from the user saying that they have read the chat - so set read status to true
      map(() => true)
    )

    let readObservable = merge(newPosts$, markedAsRead$);
    return readObservable;
  }

  subscribe(chat: JobChat): Observable<Message> {

    //Check if we already have an observable for this chat..
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
      //Not sure why need "websocket" on end of the url but you do
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

  private getMarkedChatAsReadSubject(chat: JobChat): Subject<void> {
    //todo Look up from chat in map
    return this.userMarkedChatAsRead$;
  }

  markChatAsRead(chat: JobChat) {
    const markChatAsRead$ = this.getMarkedChatAsReadSubject(chat);
    markChatAsRead$.next();
  }
}

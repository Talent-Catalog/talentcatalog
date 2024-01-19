import {Injectable, OnDestroy} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {merge, Observable, of, Subject, Subscription} from "rxjs";
import {CreateChatRequest, JobChat, JobChatUserInfo} from "../model/chat";
import {RxStompService} from "./rx-stomp.service";
import {Message} from "@stomp/stompjs";
import {map, switchMap, takeUntil, tap} from "rxjs/operators";
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
   * Map of Chat id to Chat Read Status observable - this is constructed from the chatPosts and
   * markAsReads - constructing an observable that can notify of changes to whether a user has
   * fully read a chat.
   */
  private chatReadStatuses$: Map<number, Observable<boolean>> = new Map<number, Observable<boolean>>();

  private chatReadStatuses: Map<number, boolean> = new Map<number, boolean>();

  /**
   * Map of Chat id to Observable for that chat - this is where posts come in from server
   */
  private chatPosts: Map<number, Observable<Message>> = new Map<number, Observable<Message>>();

  /**
   * The same request should always return the same chat - and chats are really jut id's that don't
   * change so we can cache them - which is what this Map does.
   * <p/>
   * Note however that although Typescript allows Objects as keys, the hash function uses the
   * objects location in memory - which is no good for us here. So instead we convert the
   * request in to string (using JSON.stringify) and use that as the key.
   * See, for example, https://stackoverflow.com/questions/63948352/typescript-map-with-objects-as-keys
   * @private
   */
  private chatByChatRequest: Map<string, JobChat> = new Map();

  /**
   * Map of Chat id to MarkAsRead Subject for that chat - this is where notifications come in
   * (locally) from user saying that they have read a chat.
   */
  private markAsReads: Map<number, Subject<boolean>> = new Map<number, Subject<boolean>>();

  private authenticationServiceSubscription: Subscription = null;

  constructor(
    private authenticationService: AuthenticationService,
    private http: HttpClient,
    private rxStompService: RxStompService
  ) {

    //Subscribe to authentication service so that we can detect logouts and disconnect on a logout.
    this.authenticationServiceSubscription = this.authenticationService.loggedInUser$.subscribe(
      (user) => {
        if (user == null) {
          //Clean up chat on logout - ie when loggedInUser becomes null.
          this.cleanUp();
        }
      }
    )
  }

  ngOnDestroy(): void {
    //Note that there seems to be some doubt whether services are ever actually destroyed.
    //See https://github.com/angular/angular/issues/37095#issuecomment-854792361
    //I could never get this method to fire from with Intellij - JC.
    //Note that we also call this.cleanup on a logout so that should tidy things up anyway.
    this.cleanUp();
  }

  private cleanUp() {
    this.disconnect();
    if (this.authenticationServiceSubscription) {
      this.authenticationServiceSubscription.unsubscribe();
    }

    this.completeMarkAsReads();

    //Clean up data structures.
    this.chatReadStatuses$.clear();
    this.chatReadStatuses.clear();
    this.chatPosts.clear();
    this.chatByChatRequest.clear();

  }

  create(request: CreateChatRequest): Observable<JobChat> {
    return this.http.post<JobChat>(`${this.apiUrl}`, request);
  }

  getOrCreate(request: CreateChatRequest): Observable<JobChat> {

    //Typescript Maps hash on object reference (!), so can't use actual request object as key to map
    const requestKey = JSON.stringify(request);

    //Check if we have already fetched the chat matching this request - if so return cached value
    const chat = this.chatByChatRequest.get(requestKey);
    if (chat) {
      return of(chat);
    } else {
      return this.http.post<JobChat>(`${this.apiUrl}/get-or-create`, request).pipe(
        tap(chat => this.chatByChatRequest.set(requestKey, chat))
      );
    }
  }

  getJobChatUserInfo(chat: JobChat): Observable<JobChatUserInfo> {
    const user = this.authenticationService.getLoggedInUser();
    return this.http.get<JobChatUserInfo>(
      `${this.apiUrl}/${chat.id}/user/${user.id}/get-chat-user-info`)
  }

  list(): Observable<JobChat[]> {
    return this.http.get<JobChat[]>(`${this.apiUrl}`)
  }

  private markAsReadUpto(chat: JobChat): Observable<void> {
    const postId = 0;
    //If we already have the data return it, otherwise get it.
    return this.http.put<void>(`${this.apiUrl}/${chat.id}/post/${postId}/read`, null)
  }

  markChatAsRead(chat: JobChat) {
    this.storeChatReadStatus(chat, true);
    const markChatAsRead$ = this.getMarkedChatAsReadSubject(chat);
    markChatAsRead$.next(true);
    this.markAsReadUpto(chat).subscribe({
      error: error => {console.log("ChatService.markAsReadUpto: Error " + error )}
    })
  }

  getChatReadStatusObservable(chat: JobChat): Observable<boolean> {
    //Check if we already have one for this chat...
    let chatReadStatus$ = this.chatReadStatuses$.get(chat.id);
    if (chatReadStatus$ == null) {
      chatReadStatus$ = this.constructChatReadStatus(chat);
      //Save observable for this chat.
      this.chatReadStatuses$.set(chat.id, chatReadStatus$);
    }

    return chatReadStatus$;
  }

  private constructChatReadStatus(chat: JobChat): Observable<boolean> {
    //New post events coming from server - set the read status to false
    let newPosts$ = this.watchChat(chat).pipe(
      //New posts set the chat read status to false
      map(message => false),
    )

    //Events signalling that user has read the chat
    const userMarkedChatAsRead$ =
      //Get Chat User Info from server
      this.getJobChatUserInfo(chat).pipe(
        //Map JobChatUser into a boolean indicating whether they have read the chat or not
        map(info => {
          let isRead: boolean;
          if (info.lastPostId == null) {
            //Chat has no posts, mark as read
            isRead = true
          } else if (info.lastReadPostId == null) {
            //User has never marked as read. Return false.
            isRead = false
          } else {
            //Read if user has read up to last post
            isRead = info.lastReadPostId >= info.lastPostId;
          }
          return isRead;
        }),

        //Initialize chat read status
        tap(isRead => this.storeChatReadStatus(chat, isRead)),

        //Now that we know whether the user has read the chat or not, switch to
        //our marked as read subject, initializing it with the isRead state returned from the server
        switchMap( isRead => {
          let subject$ = this.getMarkedChatAsReadSubject(chat);
          subject$.next(isRead);
          return subject$;
        })
      );

    //The read status of a chat is driven by the above two streams.
    //One which is driven by incoming posts to the chat and the other driven by the user clicking
    //on the chat's Mark as Read button.
    //Merge these two boolean streams - indicating the current read status of the chat.
    let chatReadStatus$ = merge(newPosts$, userMarkedChatAsRead$).pipe(
      takeUntil(this.destroyStompSubscriptions$)
    );
    return chatReadStatus$;
  }

  watchChat(chat: JobChat): Observable<Message> {

    //Check if we already have an observable for this chat..
    let observable = this.chatPosts.get(chat.id);
    if (observable == null) {

      //Not yet subscribed to this chat - subscribe and save the observable.
      this.configureStompService();

      observable = this.rxStompService.watch('/topic/chat/' + chat.id)
      //This pipe allows us to keep track of subscriptions so that we can unsubscribe on destroy
      //See https://www.learnrxjs.io/learn-rxjs/operators/filtering/takeuntil
      .pipe(takeUntil(this.destroyStompSubscriptions$));

      //Save observable for this chat.
      this.chatPosts.set(chat.id, observable);

      //Now that we are watching for chat posts, create a ChatReadStatus for the chat and subscribe
      //internally to it here just to keep this.chatReadStatuses up to date - which drive the
      //isChatRead method.
      this.getChatReadStatusObservable(chat).subscribe(
        (isRead) => this.storeChatReadStatus(chat, isRead)
      )
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

  private getMarkedChatAsReadSubject(chat: JobChat): Subject<boolean> {
    //Check if we already have one for this chat..
    let markAsRead = this.markAsReads.get(chat.id);
    if (markAsRead == null) {
      markAsRead = new Subject<boolean>();
      //Save observable for this chat.
      this.markAsReads.set(chat.id, markAsRead);
    }

    return markAsRead;
  }

  isChatRead(chat: JobChat): boolean {
    return this.chatReadStatuses.get(chat.id);
  }

  private storeChatReadStatus(chat: JobChat, isRead: boolean) {
    this.chatReadStatuses.set(chat.id, isRead);
  }

  private completeMarkAsReads() {
    this.markAsReads.forEach(subject => subject.complete());
    this.markAsReads.clear();
  }
}

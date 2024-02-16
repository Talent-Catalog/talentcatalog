import {Injectable, OnDestroy} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {ChatPost, CreateChatRequest, JobChat, JobChatUserInfo} from "../model/chat";
import {RxStompService} from "./rx-stomp.service";
import {Message} from "@stomp/stompjs";
import {map, share, shareReplay, takeUntil, tap} from "rxjs/operators";
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
   * These record the current read status of each chat - which supports the {@link #isChatRead}
   * method.
   * <p/>
   * Note that chatIsReads$ provide Observables for chat read status - so any time the status
   * changes they will provide a new status. But they do not remember the current status.
   *
   * @private
   */
  private chatIsReads: Map<number, boolean> = new Map<number, boolean>();

  /**
   * Map of Chat id to MarkAsRead Subject for that chat - this where notifications come in
   * for chat read status changes.
   */
  private chatIsReads$: Map<number, Subject<boolean>> = new Map<number, Subject<boolean>>();

  /**
   * Map of Chat id to Observable for that chat - this is where posts come in from server
   */
  private chatPosts$: Map<number, Observable<Message>> = new Map<number, Observable<Message>>();

  /**
   * The same request should always return the same chat - and chats are really just id's that don't
   * change so we can cache them - which is what this Map does.
   * <p/>
   * Note however that although Typescript allows Objects as keys, the hash function uses the
   * objects location in memory - which is no good for us here. So instead we convert the
   * request in to string (using JSON.stringify) and use that as the key.
   * See, for example, https://stackoverflow.com/questions/63948352/typescript-map-with-objects-as-keys
   * @private
   */
  private chatByRequest$: Map<string, Observable<JobChat>> = new Map();

  constructor(
    private authenticationService: AuthenticationService,
    private http: HttpClient,
    private rxStompService: RxStompService
  ) {}

  ngOnDestroy(): void {
    //Note that there seems to be some doubt whether this is called when a service is destroyed.
    //For example when a browser is refreshed - the constructor gets called on what is clearly a new
    //service, but ngDestroy was never called on the previous service instance.
    //See https://github.com/angular/angular/issues/37095#issuecomment-854792361
    //I could never get this method to fire from with Intellij - JC.
    //Note that we also call this.cleanup on a logout so that should tidy things up anyway.
    this.cleanUp();
  }

  cleanUp() {
    this.disconnect();

    this.completeMarkAsReads();

    //Clean up data structures.
    this.chatIsReads.clear();
    this.chatPosts$.clear();
    this.chatByRequest$.clear();

  }

  create(request: CreateChatRequest): Observable<JobChat> {
    return this.http.post<JobChat>(`${this.apiUrl}`, request);
  }

  getOrCreate(request: CreateChatRequest): Observable<JobChat> {

    //Typescript Maps hash on object reference (!), so can't use actual request object as key to map
    const requestKey = JSON.stringify(request);

    //Check if we have already fetched the chat matching this request - if so return cached value
    let chat$ = this.chatByRequest$.get(requestKey);
    if (chat$ == null) {
      chat$ = this.http.post<JobChat>(`${this.apiUrl}/get-or-create`, request).pipe(
        //This allows the return from the get/create server call to be shared.
        shareReplay(1)
      );
      this.chatByRequest$.set(requestKey, chat$);
    }
    return chat$;
  }

  getChatPosts$(chat:JobChat): Observable<ChatPost> {
     return this.watchChat(chat).pipe(
       map((message: Message) => {
        const payload: ChatPost = JSON.parse(message.body);
        return payload;
       }),
       tap(() => this.changeChatReadStatus(chat,false))
     );
  }


  private getJobChatUserInfo(chat: JobChat): Observable<JobChatUserInfo> {
    // console.log('Browser requests server for status of chat ' + chat.id);
    const user = this.authenticationService.getLoggedInUser();
    return this.http.get<JobChatUserInfo>(
      `${this.apiUrl}/${chat.id}/user/${user.id}/get-chat-user-info`)
  }

  list(): Observable<JobChat[]> {
    return this.http.get<JobChat[]>(`${this.apiUrl}`)
  }

  private markAsReadUptoOnServer(chat: JobChat): Observable<void> {
    const postId = 0;
    //If we already have the data return it, otherwise get it.
    return this.http.put<void>(`${this.apiUrl}/${chat.id}/post/${postId}/read`, null)
  }

  markChatAsRead(chat: JobChat) {
    this.changeChatReadStatus(chat, true);
    this.markAsReadUptoOnServer(chat).subscribe({
      error: error => {console.log("ChatService.markAsReadUpto: Error " + error )}
    })
  }

  private watchChat(chat: JobChat): Observable<Message> {

    //Check if we already have an observable for this chat...
    let observable = this.chatPosts$.get(chat.id);
    if (observable == null) {
      //Not yet subscribed to this chat - subscribe and save the observable.
      this.configureStompService();

      observable = this.rxStompService.watch('/topic/chat/' + chat.id)
      .pipe(

        //Want this to be shareable among multiple subscribers
        share(),

        //Keep track of subscriptions so that we can unsubscribe on destroy
        //See https://www.learnrxjs.io/learn-rxjs/operators/filtering/takeuntil
        takeUntil(this.destroyStompSubscriptions$),
        tap( message => console.log('Post received from server for chat ' + chat.id
        + ': ' + JSON.stringify(message.body)))
      );

      if (this.chatPosts$.has(chat.id)) {
        console.log('Multiple watch observables for chat ' + chat.id);
      }
      //Save observable for this chat.
      this.chatPosts$.set(chat.id, observable);
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

    const token = this.authenticationService.getToken();
    if (token) {
      config.connectHeaders.Authorization = `Bearer ${token}`
    }

    return config;
  }

  private computeIsReadFromJobChatUserInfo(info: JobChatUserInfo) {
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
  }

  getChatIsRead$(chat: JobChat): Observable<boolean> {
    const subject = this.getChatIsReadSubject(chat);

    if (this.isChatRead(chat) == null) {
      //If we don't know the status, we need to get it from the server.
      this.getJobChatUserInfo(chat).subscribe({
          next: info => {
            //Keep this.chatReadStatuses up to date - which drive the isChatRead method.
            const isRead = this.computeIsReadFromJobChatUserInfo(info);
            this.changeChatReadStatus(chat, isRead);
          },
          error: err => {console.log(err)}
        }
      );
    }

    return subject;
  }

  private getChatIsReadSubject(chat: JobChat): Subject<boolean> {
    //Check if we already have one for this chat..
    let chatIsRead = this.chatIsReads$.get(chat.id);
    if (chatIsRead == null) {
      chatIsRead = new Subject<boolean>();
      //Save observable for this chat.
      this.chatIsReads$.set(chat.id, chatIsRead);
    }

    return chatIsRead;
  }

  /**
   * This returns the current read status of the given chat (Observables don't give you that -
   * only changes).
   * <p/>
   * This is needed by code that wants to display the current read status now - without waiting
   * for the next status change to come through on an Observable.
   * @param chat Chat whose current read status is wanted
   * @return True or False - or undefined if we don't know.
   */
  isChatRead(chat: JobChat): boolean {
    return this.chatIsReads.get(chat.id);
  }

  private changeChatReadStatus(chat: JobChat, isRead: boolean) {
    this.getChatIsReadSubject(chat).next(isRead);
    this.chatIsReads.set(chat.id, isRead);
  }

  private completeMarkAsReads() {
    this.chatIsReads$.forEach(subject => subject.complete());
    this.chatIsReads$.clear();
  }

  removeDuplicateChats(jobChats: JobChat[]) {
    let filterMap: Map<number, JobChat> = new Map<number, JobChat>();
    for (const jobChat of jobChats) {
      filterMap.set(jobChat.id, jobChat);
    }
    return Array.from(filterMap.values());
  }
}

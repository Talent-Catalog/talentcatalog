import {Injectable, OnDestroy} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {merge, Observable, of, Subject} from "rxjs";
import {ChatPost, CreateChatRequest, JobChat, JobChatUserInfo} from "../model/chat";
import {RxStompService} from "./rx-stomp.service";
import {Message} from "@stomp/stompjs";
import {map, share, switchMap, takeUntil, tap} from "rxjs/operators";
import {RxStompConfig} from "@stomp/rx-stomp";
import {AuthenticationService} from "./authentication.service";
import {Job} from "../model/job";

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

  /**
   * These record the current read status of each chat - which supports the {@link #isChatRead}
   * method.
   * <p/>
   * Note that chatReadStatuses$ provide Observables for chat read status - so any time the status
   * changes they will provide a new status. But they do not remember the current status.
   *
   * @private
   */
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
  private chatIsReads$: Map<number, Subject<boolean>> = new Map<number, Subject<boolean>>();

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
    console.log('Browser requests server for status of chat ' + chat.id);
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

  private getChatReadStatusObservable(chat: JobChat): Observable<boolean> {
    //Check if we already have one for this chat...
    let chatReadStatus$ = this.chatReadStatuses$.get(chat.id);
    if (chatReadStatus$ == null) {

      const userMarkedAsRead$ = this.constructChatReadStatusFromServer(chat);

      //todo There is a window here where a second get for the same chat will start a new
      console.log('Constructing observable for chat ' + chat.id)

      //First subscriber (above) gets userMarkedAsRead, which will eventually switch to a merged
      //Observable of userMarkedAsReadSubject and newPosts
      //read from the server
      chatReadStatus$ = userMarkedAsRead$;
    }

    return chatReadStatus$;
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

  private constructChatReadStatusFromServer(chat: JobChat): Observable<boolean> {
    //Events signalling that user has read the chat
    const userMarkedChatAsRead$ =
      //Get Chat User Info from server
      this.getJobChatUserInfo(chat).pipe(
        //Map JobChatUser into a boolean indicating whether they have read the chat or not
        map(info => {
          console.log("User info received from server for chat " + chat.id + ' ' + JSON.stringify(info));
          return this.computeIsReadFromJobChatUserInfo(info);
        }),

        //Allow the new status from the server to be multicast
        share(),

        //Now that we know whether the user has read the chat or not, switch to an Observable
        //merged from our marked as read subject and new posts
        //our marked as read subject, initializing it with the isRead state returned from the server
        switchMap( isRead => {
          let subject$ = this.getChatIsReadSubject(chat);
          subject$.next(isRead);

          console.log('Server status for chat ' + chat.id);
          return this.constructChatReadStatus(chat, subject$);
        })
      );

    return userMarkedChatAsRead$;
  }

  private constructChatReadStatus(
    chat: JobChat, markedChatAsReadSubject$: Observable<boolean>): Observable<boolean> {
    //New post events coming from server - set the read status to false
    let newPosts$ = this.watchChat(chat).pipe(
      //New posts set the chat read status to false
      map(message => false),
      tap(isRead => console.log('Read status set by incoming post for chat ' + chat.id))
    )

    //The read status of a chat is driven by the above two streams.
    //One which is driven by incoming posts to the chat and the other driven by the user clicking
    //on the chat's Mark as Read button.
    //Merge these two boolean streams - indicating the current read status of the chat.
    let chatReadStatus$ = merge(newPosts$, markedChatAsReadSubject$).pipe(
      takeUntil(this.destroyStompSubscriptions$),
      tap( isRead =>
        console.log('Status received through merged observable for chat ' + chat.id))
    );

    //This is what subsequent subscribers will hook into
    if (!this.chatReadStatuses$.has(chat.id)) {
      console.log('Constructed observable for chat ' + chat.id)
      this.chatReadStatuses$.set(chat.id, chatReadStatus$)
    } else {
      console.log('ERROR: duplicate observable created for chat ' + chat.id)
    }

    return chatReadStatus$;
  }

  private watchChat(chat: JobChat): Observable<Message> {

    //Check if we already have an observable for this chat...
    let observable = this.chatPosts.get(chat.id);
    if (observable == null) {
      //todo Plug this with temp Observable - could concat with final Obseravble
      //Not yet subscribed to this chat - subscribe and save the observable.
      this.configureStompService();

      observable = this.rxStompService.watch('/topic/chat/' + chat.id)
      .pipe(

        //todo Is this observable already multicast.
        //todo Do we want to unsubscribe when ref coount gets to zero. If we do,
        //how does rxStompService handle that- and we also need to clear the this.chatPosts for this
        //chat so that we resubscribe.
        share(),

        //Keep track of subscriptions so that we can unsubscribe on destroy
        //See https://www.learnrxjs.io/learn-rxjs/operators/filtering/takeuntil
        takeUntil(this.destroyStompSubscriptions$),
        tap( message => console.log('Post received from server for chat ' + chat.id
        + ': ' + JSON.stringify(message.body)))
      );

      if (this.chatPosts.has(chat.id)) {
        console.log('Multiple watch observables for chat ' + chat.id);
      }
      //Save observable for this chat.
      this.chatPosts.set(chat.id, observable);
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
    return this.chatReadStatuses.get(chat.id);
  }

  private changeChatReadStatus(chat: JobChat, isRead: boolean) {
    this.getChatIsReadSubject(chat).next(isRead);
    this.chatReadStatuses.set(chat.id, isRead);
  }

  private completeMarkAsReads() {
    this.chatIsReads$.forEach(subject => subject.complete());
    this.chatIsReads$.clear();
  }
}

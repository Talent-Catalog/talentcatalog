import {Injectable, OnDestroy} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable, Subject} from "rxjs";
import {JobChat, UpdateChatRequest} from "../model/chat";
import {AuthService} from "./auth.service";
import {RxStompService} from "./rx-stomp.service";
import {Message} from "@stomp/stompjs";
import {takeUntil} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ChatService implements OnDestroy {

  private apiUrl: string = environment.apiUrl + '/chat';
  private stompServiceConfigured = false;
  private destroyStompSubscriptions$ = new Subject<void>();
  private observables: Map<number, Observable<Message>> = new Map<number, Observable<Message>>();

  constructor(
      private authService: AuthService,
      private http: HttpClient,
      private rxStompService: RxStompService
  ) {
  }

  ngOnDestroy(): void {
    this.unsubscribeAll();
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

  unsubscribeAll() {
    //Unsubscribe all stomp subscriptions
    //See https://www.learnrxjs.io/learn-rxjs/operators/filtering/takeuntil
    this.destroyStompSubscriptions$.next();
    this.destroyStompSubscriptions$.complete();
  }

  private configureStompService() {
    //Check if already configured
    if (!this.stompServiceConfigured) {
      this.rxStompService.configure(this.authService.getRxStompConfig());
      this.rxStompService.activate();
      this.stompServiceConfigured = true;
    }
  }
}

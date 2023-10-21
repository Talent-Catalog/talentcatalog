import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {JobChat, UpdateChatRequest} from "../model/chat";

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl: string = environment.apiUrl + '/chat';

  constructor(private http: HttpClient) { }

  create(request: UpdateChatRequest): Observable<JobChat> {
    return this.http.post<JobChat>(`${this.apiUrl}`, request);
  }

}

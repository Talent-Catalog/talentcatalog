import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ChatPost} from "../model/chat";

@Injectable({
  providedIn: 'root'
})
export class ChatPostService {
  private apiUrl: string = environment.chatApiUrl + '/chat-post';

  constructor(private http: HttpClient) { }

  listPosts(chatId: number): Observable<ChatPost[]> {
    //If we already have the data return it, otherwise get it.
    return this.http.get<ChatPost[]>(`${this.apiUrl}/${chatId}/list`)
  }

  uploadFile(chatId: number, formData): Observable<String> {
    //We just want the link of the file returned, we don't need the object.
    return this.http.post<String>(`${this.apiUrl}/${chatId}/upload`, formData)
  }

}

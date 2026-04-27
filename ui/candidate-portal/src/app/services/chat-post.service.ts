/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ChatPost} from "../model/chat";
import {UrlDto} from "../model/url-dto";

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

  uploadFile(chatId: number, formData): Observable<UrlDto> {
    //We just want the link of the file returned, we don't need the object.
    return this.http.post<UrlDto>(`${this.apiUrl}/${chatId}/upload`, formData)
  }

}

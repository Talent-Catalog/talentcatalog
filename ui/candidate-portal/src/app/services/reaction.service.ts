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

import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Reaction} from "../model/reaction";

export interface AddReactionRequest {
    emoji: string;
}

@Injectable({
    providedIn: 'root'
})
export class ReactionService {
    private apiUrl: string = environment.chatApiUrl + '/reaction';

    constructor(private http: HttpClient) { }

    addReaction(chatPostId: number, request: AddReactionRequest): Observable<Reaction[]> {
        return this.http.post<Reaction[]>(`${this.apiUrl}/${chatPostId}/add-reaction`, request);
    }

    modifyReaction(chatPostId: number, id: number): Observable<Reaction[]> {
        return this.http.put<Reaction[]>(`${this.apiUrl}/${chatPostId}/modify-reaction/${id}`, null);
    }
}

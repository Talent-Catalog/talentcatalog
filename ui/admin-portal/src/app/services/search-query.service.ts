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
import {BehaviorSubject, Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SearchQueryService {
  private searchTermsSource = new BehaviorSubject<string[]>([]);

  constructor() {}

  changeSearchQuery(query: string) {
    if (query && query.length > 0) {
      try {
        const queryArray = this.parseSearchQuery(query);
        this.searchTermsSource.next(queryArray);
      } catch (error) {
        console.error('Failed to parse search query: ' + query, error);
      }
    } else {
      this.searchTermsSource.next([]);
    }
  }

  get currentSearchTerms(): Observable<string[]> {
    return this.searchTermsSource.asObservable();
  }

  /*
   * Parses the input search query string based on elastic search syntax. Extracts and returns an
   * array of search terms and phrases.
   *
   * Example input: 'accountant + (excel powerpoint) "hospital director"'
   * Example output: ["accountant", "excel", "powerpoint", "hospital director"]
   *
   * @param {string} query - The search query string entered by the user.
   * @returns {string[]} An array of search terms and phrases extracted from the input query.
   */
  private parseSearchQuery(query): string[] {
    // Handle phrases in quotes as a single term
    const phrases = [...query.matchAll(/"([^"]+)"/g)].map(match => match[1]);

    // Remove the phrases from the query to simplify splitting the rest
    let modifiedQuery = query.replace(/"[^"]+"/g, '');

    // Remove brackets from bracketed expressions
    modifiedQuery = modifiedQuery.replace(/\(([^)]+)\)/g, '$1');

    // Split by space for OR, and plus for AND, then filter out empty strings and operators
    const words = modifiedQuery.split(/\s+|\++/).filter(token => token && token !== '+');

    // Combine words and phrases
    const wordsAndPhrases = words.concat(phrases);

    // Handle wildcard: Remove '*' from the terms
    const searchTerms = wordsAndPhrases.map(term => term.replace(/\*/g, ''));

    // Sort terms by length, from longest to shortest
    return searchTerms.sort((a, b) => b.length - a.length);
  }

}

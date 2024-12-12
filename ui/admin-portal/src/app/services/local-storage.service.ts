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

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {
  private readonly prefix = 'tc-admin-';

  private addPrefix(key: string): string {
    return `${this.prefix}${key}`;
  }

  /**
   * Retrieves an item from local storage.
   * @param key - The key of the item to retrieve.
   * @returns The parsed item or `null` if the item does not exist.
   */
  get<T>(key: string): T | null {
    const item = localStorage.getItem(this.addPrefix(key));
    if (item) {
      try {
        return JSON.parse(item) as T;
      } catch (error) {
        console.error(`Error parsing JSON for key "${key}":`, error);
        return null;
      }
    }
    return null;
  }

  /**
   * Stores an item in local storage.
   * @param key - The key to store the item under.
   * @param value - The value to store, which will be serialized as JSON.
   */
  set<T>(key: string, value: T): void {
    try {
      localStorage.setItem(this.addPrefix(key), JSON.stringify(value));
    } catch (error) {
      console.error(`Error setting item in localStorage: ${error}`);
    }
  }

  /**
   * Removes an item from local storage.
   * @param key - The key of the item to remove.
   */
  remove(key: string): void {
    try {
      localStorage.removeItem(this.addPrefix(key));
    } catch (error) {
      console.error(`Error removing item with key "${key}" from localStorage:`, error);
    }
  }

  /**
   * Clears all items with the specified prefix from local storage.
   */
  clear(): void {
    Object.keys(localStorage)
    .filter(key => key.startsWith(this.prefix))
    .forEach(key => localStorage.removeItem(key));
  }
}

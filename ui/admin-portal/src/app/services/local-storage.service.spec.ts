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

import {TestBed} from '@angular/core/testing';
import {LocalStorageService} from "./local-storage.service";


describe('LocalStorageService', () => {
  let service: LocalStorageService;
  const prefix = 'tc-admin-';
  const testKey = 'testKey';
  const testValue = { name: 'Abdullah', role: 'admin' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageService]
    });
    service = TestBed.inject(LocalStorageService);

    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('#setItem', () => {
    it('should store an item in localStorage with the prefixed key', () => {
      service.set(testKey, testValue);
      const storedValue = localStorage.getItem(prefix + testKey);
      expect(storedValue).toEqual(JSON.stringify(testValue));
    });
  });

  describe('#getItem', () => {
    it('should retrieve an item from localStorage by the prefixed key', () => {
      localStorage.setItem(prefix + testKey, JSON.stringify(testValue));
      const result = service.get<typeof testValue>(testKey);
      expect(result).toEqual(testValue);
    });

    it('should return null if the item does not exist in localStorage', () => {
      const result = service.get<typeof testValue>('nonExistentKey');
      expect(result).toBeNull();
    });
  });

  describe('#removeItem', () => {
    it('should remove an item from localStorage by the prefixed key', () => {
      localStorage.setItem(prefix + testKey, JSON.stringify(testValue));
      service.remove(testKey);
      const storedValue = localStorage.getItem(prefix + testKey);
      expect(storedValue).toBeNull();
    });
  });

  describe('#clear', () => {
    it('should remove only items with the specified prefix from localStorage', () => {
      localStorage.setItem(prefix + 'item1', 'value1');
      localStorage.setItem(prefix + 'item2', 'value2');
      localStorage.setItem('unprefixedItem', 'value3');

      service.clear();

      expect(localStorage.getItem(prefix + 'item1')).toBeNull();
      expect(localStorage.getItem(prefix + 'item2')).toBeNull();
      expect(localStorage.getItem('unprefixedItem')).toEqual('value3');
    });
  });
});

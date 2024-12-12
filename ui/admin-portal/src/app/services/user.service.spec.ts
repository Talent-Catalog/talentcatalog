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
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {UserService} from './user.service';
import {environment} from '../../environments/environment';
import {User, UpdateUserRequest} from '../model/user';
import {SearchUserRequest} from '../model/base';
import {MockUser} from "../MockData/MockUser";

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  const mockUser: User = new MockUser();

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should call search and return a list of users', () => {
    const mockUsers: User[] = [new MockUser(), new MockUser()];

    const searchRequest: SearchUserRequest = {keyword: 'test'};

    service.search(searchRequest).subscribe(users => {
      expect(users.length).toBe(2);
      expect(users).toEqual(mockUsers);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/user/search`);
    expect(req.request.method).toBe('POST');
    req.flush(mockUsers);
  });

  it('should call get and return a user', () => {
    service.get(1).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/user/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('should call create and return the created user', () => {
    const updateUserRequest: UpdateUserRequest = {username: 'user1', firstName: 'First1', lastName: 'Last1', role: 'user'} as UpdateUserRequest;

    service.create(updateUserRequest).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/user`);
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);
  });

  it('should call update and return the updated user', () => {
    const updateUserRequest: UpdateUserRequest = {username: 'user1', firstName: 'First1', lastName: 'Last1', role: 'user'} as UpdateUserRequest;

    service.update(1, updateUserRequest).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/user/1`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockUser);
  });

  it('should call delete and return true', () => {
    service.delete(1).subscribe(response => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/user/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });


});

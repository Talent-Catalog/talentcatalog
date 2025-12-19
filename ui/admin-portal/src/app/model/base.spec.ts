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
import {User} from './user';
import {AuthenticationService} from '../services/authentication.service';
import {EMAIL_REGEX, findHasId, indexOfHasId, salesforceUrlRegExp} from "./base";
import {MockUser} from "../MockData/MockUser";


describe('Miscellaneous Tests', () => {
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let mockUser: User;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    TestBed.configureTestingModule({
      providers: [
        {provide: AuthenticationService, useValue: spy}
      ]
    });
    authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;

    mockUser = new MockUser();
  });

  it('should return the index of a HasId object', () => {
    const hasIds = [{ id: 1 }, { id: 2 }, { id: 3 }];
    expect(indexOfHasId(2, hasIds)).toBe(1);
    expect(indexOfHasId(4, hasIds)).toBe(-1);
  });

  it('should find a HasId object', () => {
    const hasIds = [{ id: 1 }, { id: 2 }, { id: 3 }];
    expect(findHasId(2, hasIds)).toEqual({ id: 2 });
    expect(findHasId(4, hasIds)).toBeNull();
  });

  it('should validate email regex', () => {
    const validEmails = ['test@example.com', 'user.name+tag+sorting@example.com', 'user@sub.domain.com'];
    const invalidEmails = ['plainaddress', '@missingusername.com', 'username@.com', 'username@.com.'];

    validEmails.forEach(email => {
      expect(new RegExp(EMAIL_REGEX).test(email)).toBeTrue();
    });

    invalidEmails.forEach(email => {
      expect(new RegExp(EMAIL_REGEX).test(email)).toBeFalse();
    });
  });

  it('should validate Salesforce URL regex', () => {
    const validUrls = [
      'https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/123456789012345',
      'https://talentbeyondboundaries.lightning.force.com/lightning/r/Account/123456789012345'
    ];
    const invalidUrls = [
      'https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/',
      'https://talentbeyondboundaries.lightning.force.com/lightning/r/123456789012345',
      'https://example.com/lightning/r/Opportunity/123456789012345',
      'https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/123'
    ];

    validUrls.forEach(url => {
      expect(salesforceUrlRegExp.test(url)).toBeTrue();
    });

    invalidUrls.forEach(url => {
      expect(salesforceUrlRegExp.test(url)).toBeFalse();
    });
  });

});

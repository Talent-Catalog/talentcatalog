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

import {User, roleGreaterThan, Role} from './user';

describe('User Class', () => {
  let user: User;

  beforeEach(() => {
    user = new User();
  });

  it('should create an instance of User', () => {
    expect(user).toBeTruthy();
    expect(user instanceof User).toBe(true);
  });

  it('should correctly determine role hierarchy', () => {
    // Test cases based on the role hierarchy defined in Role enum

    // Role.admin should be greater than Role.systemadmin
    expect(roleGreaterThan(Role.admin, Role.systemadmin)).toBe(false);

    // Role.partneradmin should not be greater than Role.admin
    expect(roleGreaterThan(Role.partneradmin, Role.admin)).toBe(false);
    //
    // // Role.limited should not be greater than Role.semilimited
    expect(roleGreaterThan(Role.limited, Role.semilimited)).toBe(false);
    //
    // // Role.systemadmin is greater than any other role
    expect(roleGreaterThan(Role.systemadmin, Role.admin)).toBe(true);
    expect(roleGreaterThan(Role.systemadmin, Role.limited)).toBe(true);
    //
    // // Role.semilimited is not greater than Role.partneradmin
    expect(roleGreaterThan(Role.semilimited, Role.partneradmin)).toBe(false);
    //
    // // Edge case: Role.admin is not greater than itself
    expect(roleGreaterThan(Role.admin, Role.admin)).toBe(false);
  });
});


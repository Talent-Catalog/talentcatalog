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

import {JwtAuthenticationResponse} from "./jwt-authentication-response";
import {User} from "./user";
import {MockUser} from "../MockData/MockUser";
import {TcInstanceType} from "./tc-instance-type";

describe('JwtResponse', () => {
  it('should create a valid JwtResponse object', () => {
    // Mock data
    const mockUser: User = new MockUser();

    const jwtResponse: JwtAuthenticationResponse = {
      accessToken: 'mockAccessToken',
      tcInstanceType: TcInstanceType.TBB,
      tokenType: 'Bearer',
      user: mockUser,
      canViewChats: true,
    };

    // Assertions
    expect(jwtResponse.accessToken).toEqual('mockAccessToken');
    expect(jwtResponse.tokenType).toEqual('Bearer');
    expect(jwtResponse.canViewChats).toEqual(true);

    // Validate User object structure
    expect(jwtResponse.user.id).toEqual(1);
    expect(jwtResponse.user.username).toEqual('mockuser');
    expect(jwtResponse.user.email).toEqual('john.doe@example.com');
    expect(jwtResponse.user.firstName).toEqual('John');
    expect(jwtResponse.user.lastName).toEqual('Doe');
    expect(jwtResponse.user.role).toEqual('Limited');
  });
});

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

import {User} from "./user";
import {TcInstanceType} from "./tc-instance-type";

/**
 * Response from the server in response to a successful user login or registration
 */
export interface AuthenticationResponse {

  /**
   * True if the user is authorized to view chats.
   */
  canViewChats: boolean;

  /**
   * Indicates that the user is a candidate who gave their to being contacted
   * by partners.
   * <p>
   * Note that this value is only valid in response to a candidate registration - ie when
   * {@link #userIsNew} is true. At any other time it will always be false.
   */
  contactConsentPartners: boolean;

  /**
   * Indicates that the user is a candidate who gave their to being contacted
   * related to their registration.
   * <p>
   * Note that this value is only valid in response to a candidate registration - ie when
   * {@link #userIsNew} is true. At any other time it will always be false.
   */
  contactConsentRegistration: boolean;

  /**
   * Instance of the running server.
   */
  tcInstanceType: TcInstanceType;

  /**
   * True if the returned User is new.
   * This will mean that a full registration process should start.
   */
  userIsNew: boolean;

  /**
   * Authenticated user.
   */
  user: User;
}

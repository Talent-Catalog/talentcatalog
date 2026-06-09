/*
 * Copyright (c) 2026 Talent Catalog.
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
// auth-provider.ts
import {Observable} from 'rxjs';
import {AuthStatus} from './auth-status';
import {AuthProfile} from "./auth-profile";

/**
 * Interface for an OAuth2 authentication provider - eg Keycloak or Cognito.
 */
export interface AuthProvider {
  init(): Promise<boolean>;
  isAuthenticated(): boolean;
  login(): Promise<void>;
  register(): Promise<void>;
  logout(): Promise<void>;
  getProfile(): Promise<AuthProfile>;
  getToken(): string | undefined;
  refreshToken(minValiditySeconds?: number): Promise<void>;

  getStatus(): Observable<AuthStatus>;
  getCurrentStatus(): AuthStatus;
  clearError(): void;
}

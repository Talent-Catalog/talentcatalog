/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import * as path from 'node:path';
import * as dotenv from 'dotenv';

/**
 * Loads developer-local Playwright configuration.
 *
 * CI-provided environment variables take precedence because dotenv does not
 * overwrite values that already exist in process.env by default.
 */
dotenv.config({
  path: path.resolve(process.cwd(), '.env.e2e.local'),
  override: false,
  quiet: true,
});

/**
 * Names of environment variables used by the candidate-portal E2E suite.
 */
export const E2E_ENVIRONMENT_VARIABLES = {
  baseUrl: 'E2E_BASE_URL',
  candidateUsername: 'E2E_CANDIDATE_USERNAME',
  candidatePassword: 'E2E_CANDIDATE_PASSWORD'
} as const;

/**
 * Reads a required environment variable.
 *
 * @param name environment-variable name
 * @returns trimmed environment-variable value
 * @throws Error when the variable is missing or empty
 */
export function requireEnvironmentVariable(name: string): string {
  const value = process.env[name]?.trim();

  if (!value) {
    throw new Error(
      `Missing required E2E environment variable: ${name}. ` +
      'For local runs, copy .env.e2e.example to .env.e2e.local. ' +
      'For CI runs, configure the corresponding GitHub Environment secret.',
    );
  }

  return value;
}

/**
 * Validated configuration used by Playwright tests.
 */
export interface E2EEnvironment {
  baseUrl: string;
  candidateUsername: string;
  candidatePassword: string;
}

/**
 * Returns the validated candidate-portal E2E configuration.
 *
 * This function should be called by tests that require authentication.
 *
 * @returns validated E2E configuration
 */
export function getE2EEnvironment(): E2EEnvironment {
  return {
    baseUrl:
      process.env[E2E_ENVIRONMENT_VARIABLES.baseUrl]?.trim() ||
      'http://127.0.0.1:4200',

    candidateUsername: requireEnvironmentVariable(
      E2E_ENVIRONMENT_VARIABLES.candidateUsername,
    ),

    candidatePassword: requireEnvironmentVariable(
      E2E_ENVIRONMENT_VARIABLES.candidatePassword,
    ),
  };
}

/**
 * Returns only the base URL.
 *
 * Unlike authentication credentials, the base URL has a safe local default.
 *
 * @returns configured or default candidate-portal URL
 */
export function getE2EBaseUrl(): string {
  return (
    process.env[E2E_ENVIRONMENT_VARIABLES.baseUrl]?.trim() ||
    'http://127.0.0.1:4200'
  );
}

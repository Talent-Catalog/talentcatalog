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
  candidatePassword: 'E2E_CANDIDATE_PASSWORD',
  apiBaseUrl: 'E2E_API_BASE_URL',
  verifyPlusBaselineUnhcrId:
    'E2E_VERIFY_PLUS_BASELINE_UNHCR_ID',
  verifyPlusDuplicateUnhcrId:
    'E2E_VERIFY_PLUS_DUPLICATE_UNHCR_ID',
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
    baseUrl: getE2EBaseUrl(),

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

/**
 * Returns the explicitly configured candidate-portal E2E base URL.
 *
 * Unlike {@link getE2EBaseUrl}, this function does not provide a local
 * fallback. It is useful when code needs to know whether an external E2E
 * target has actually been configured.
 *
 * @returns trimmed configured base URL, or undefined when not configured
 */
export function getConfiguredE2EBaseUrl(): string | undefined {
  const value =
    process.env[E2E_ENVIRONMENT_VARIABLES.baseUrl]?.trim();

  return value || undefined;
}
/**
 * Returns the candidate-portal backend origin.
 *
 * API tests provide the full endpoint path separately, preventing URL
 * resolution from accidentally removing path segments.
 *
 * @returns configured or default backend origin
 */
export function getE2EApiBaseUrl(): string {
  return (
    process.env[
      E2E_ENVIRONMENT_VARIABLES.apiBaseUrl
      ]?.trim() ||
    'http://localhost:8080'
  );
}


/**
 * Returns the UNHCR number owned by another active-like candidate.
 *
 * This fixture is optional for developer environments that have not prepared
 * duplicate test data. The duplicate API test reports itself as skipped when
 * no value is configured.
 *
 * @returns seeded duplicate UNHCR number, or null when not configured
 */
export function getE2EVerifyPlusDuplicateUnhcrId():
  string | null {
  const value =
    process.env[
      E2E_ENVIRONMENT_VARIABLES
        .verifyPlusDuplicateUnhcrId
      ]?.trim();

  return value || null;
}


export function getE2EVerifyPlusBaselineUnhcrId(): string {
  return requireEnvironmentVariable(
    E2E_ENVIRONMENT_VARIABLES.verifyPlusBaselineUnhcrId,
  );
}

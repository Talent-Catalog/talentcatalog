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
// auth-error.util.ts
export function describeAuthError(prefix: string, error: unknown): string {
  if (error instanceof Error && error.message) {
    return `${prefix}: ${error.message}`;
  }

  if (typeof error === 'string' && error.trim().length > 0) {
    return `${prefix}: ${error}`;
  }

  try {
    const json = JSON.stringify(error);
    if (json && json !== '{}') {
      return `${prefix}: ${json}`;
    }
  } catch {
    // ignore JSON conversion failure
  }

  return prefix;
}

export function reportAuthError(prefix: string, error: unknown): string {
  const message = describeAuthError(prefix, error);
  console.error(message, error);
  return message;
}

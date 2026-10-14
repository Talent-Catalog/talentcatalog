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

/**
 * A candidate source's matching context, as returned by the server when it is resolved: the
 * natural language description that candidates are matched against (eg refined job
 * requirements). Mirrors the server's MatchingSnapshot.
 * <p/>
 * Candidate sources taking part in the same matching activity - eg a job's searches and lists -
 * share a Matching.
 */
export interface Matching {
  id: number;
  /** The description - absent/null if the Matching has no description. */
  matchingDescription?: string;
  /**
   * When the description last changed (ISO-8601 timestamp). Anything derived from the
   * description, such as a match explanation, is out of date once this changes.
   */
  updatedDate: string;
}

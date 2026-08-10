/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import *as path from 'node:path';

/**
 * Absolute path to the authenticated candidate browser state.
 *
 * The authentication setup project creates this file before the browser and
 * device projects run. The file contains cookies and local-storage values,
 * including the Talent Catalog access token.
 *
 * This file must not be committed to source control, so we added to .gitignore.
 */
export const candidateAuthFile = path.resolve(
  process.cwd(),
  'playwright-tests/.auth/candidate.json',
);


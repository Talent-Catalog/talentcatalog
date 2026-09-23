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

import {environment} from '../../environments/environment';

/**
 * Returns true when the candidate portal runs in a non-production environment
 * where Verify+ UI should be visible.
 */
export function isLocalOrStaging(): boolean {
  return ['local', 'staging'].includes(environment.environmentName);
}

/**
 * Verify+ UI is enabled only for GRN instances in local or staging.
 * Production and TBB instances should not show Verify+ surfaces.
 */
export function isVerifyPlusUiEnabled(isGrnInstance: boolean): boolean {
  return isGrnInstance && isLocalOrStaging();
}

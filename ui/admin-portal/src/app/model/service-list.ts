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

export enum ListAction {
  REASSIGN = 'REASSIGN'
}

/**
 * Maps each ListAction to a FontAwesome icon class.
 *
 * DEVELOPER NOTE: when adding a new ListAction enum value, add a corresponding entry here.
 * The Record type enforces exhaustiveness — a missing key is a compile error.
 */
export const listActionIcons: Record<ListAction, string> = {
  [ListAction.REASSIGN]: 'fa-rotate'
};

/**
 * Maps each ListAction to a display label.
 *
 * DEVELOPER NOTE: when adding a new ListAction enum value, add a corresponding entry here.
 * The Record type enforces exhaustiveness — a missing key is a compile error.
 */
export const listActionLabels: Record<ListAction, string> = {
  [ListAction.REASSIGN]: 'Reassign'
};

/**
 * Maps each ListAction to a tooltip shown on the action button before the user clicks.
 *
 * DEVELOPER NOTE: when adding a new ListAction enum value, add a corresponding entry here.
 * The Record type enforces exhaustiveness — a missing key is a compile error.
 */
export const listActionTooltips: Record<ListAction, string> = {
  [ListAction.REASSIGN]: 'Assign new coupon to selected candidate(s)'
};

export interface ServiceList {
  id: number;
  provider: string;
  serviceCode: string;
  listRole: string;
  permittedActions: ListAction[];
}

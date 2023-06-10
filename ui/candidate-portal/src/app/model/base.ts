/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

export interface HasId {
  id?: number;
}

export interface OpportunityIds extends HasId {
  sfId?: string;
}

export interface Auditable extends HasId {
  createdDate?: number;
  updatedDate?: number;
}

export interface Opportunity extends Auditable, OpportunityIds {
  closingComments?: string;
  name: string;
  nextStep?: string;
  nextStepDueDate?: Date;

}


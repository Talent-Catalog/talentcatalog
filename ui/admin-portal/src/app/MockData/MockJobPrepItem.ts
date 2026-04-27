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


import {JobPrepItem} from "../model/job-prep-item";

export class MockJobPrepItem extends JobPrepItem {
  private _isCompleted: boolean;

  constructor(description: string, tabId: string, isCompleted: boolean) {
    super(description, tabId);
    this._isCompleted = isCompleted;
  }

  isCompleted(): boolean {
    return this._isCompleted;
  }
}

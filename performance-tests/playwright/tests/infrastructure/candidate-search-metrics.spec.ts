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
import {expect, test} from '@playwright/test';
import {median} from '../journeys/admin/candidate-search-metrics';

test('median uses the middle of three measured runs', () => {
  expect(median([340, 120, 200])).toBe(200);
});

test('median handles even counts and rejects missing measurements', () => {
  expect(median([100, 400, 300, 200])).toBe(250);
  expect(() => median([])).toThrow('without measurements');
});

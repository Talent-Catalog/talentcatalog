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

import {DataRow} from './data-row';
import {ChartType} from 'chart.js';
import {StatReport} from './stat-report';

// Mock objects
const mockDataRow: DataRow = {
  label:'test',
  value:1
};
const mockChartType: ChartType = 'bar'; // or any valid ChartType

describe('StatReport Interface', () => {
  it('should have a name property of type string', () => {
    const statReport: StatReport = {
      name: 'Sample Report',
      rows: [mockDataRow],
      chartType: mockChartType,
    };

    expect(typeof statReport.name).toBe('string');
    expect(statReport.name).toBe('Sample Report');
  });

  it('should have a rows property of type array', () => {
    const statReport: StatReport = {
      name: 'Sample Report',
      rows: [mockDataRow],
      chartType: mockChartType,
    };

    expect(Array.isArray(statReport.rows)).toBe(true);
    expect(statReport.rows.length).toBe(1);
    expect(statReport.rows[0]).toBe(mockDataRow);
  });

  it('should have a chartType property of type ChartType', () => {
    const statReport: StatReport = {
      name: 'Sample Report',
      rows: [mockDataRow],
      chartType: mockChartType,
    };

    expect(typeof statReport.chartType).toBe('string'); // Since ChartType is a string-based enum
    expect(statReport.chartType).toBe(mockChartType);
  });
});

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

import {ChartComponent} from "./chart.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {DataRow} from "../../../model/data-row";
import {NgChartsModule} from "ng2-charts";

describe('ChartComponent', () => {
  let component: ChartComponent;
  let fixture: ComponentFixture<ChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgChartsModule],
      declarations: [ChartComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize doughnut chart correctly', () => {
    const chartData: DataRow[] = [
      { label: 'Label 1', value: 10 },
      { label: 'Label 2', value: 20 },
      { label: 'Label 3', value: 30 }
    ];

    component.chartData = chartData;
    component.chartType = 'doughnut';
    component.chartLegend = true;

    component.ngOnInit();
    expect(component.chartLabels).toEqual(['Label 1', 'Label 2', 'Label 3']);
    expect(component.chartDataSet).toEqual({
      labels: ['Label 1', 'Label 2', 'Label 3'],
      datasets: [
        { data: [10, 20, 30] }
      ]
    });
    expect(component.chartOptions).toEqual({ responsive: true });
  });

  it('should initialize bar chart correctly', () => {
    const chartData: DataRow[] = [
      { label: 'Label 1', value: 10 },
      { label: 'Label 2', value: 20 },
      { label: 'Label 3', value: 30 }
    ];

    component.chartData = chartData;
    component.chartType = 'bar';
    component.chartLegend = false;

    component.ngOnInit();

    expect(component.chartLabels).toEqual(['Label 1', 'Label 2', 'Label 3']);
    expect(component.chartDataSet).toEqual({
      labels: ['Label 1', 'Label 2', 'Label 3'],
      datasets: [
        { data: [10, 20, 30] }
      ]
    });
    expect(component.chartOptions).toEqual({
      responsive: true,
      scales: {
        y: {
          beginAtZero: true,
          min: 0
        }
      }
    });
  });

  it('should not initialize chart with data if chartData is not provided', () => {
    component.chartType = 'doughnut';
    component.chartLegend = true;

    component.ngOnInit();

    expect(component.chartLabels).toEqual([]);
    expect(component.chartDataSet).toEqual({
      labels: [],
      datasets: [
        { data: [] }
      ]
    });
    expect(component.chartOptions).toEqual({});
  });
});

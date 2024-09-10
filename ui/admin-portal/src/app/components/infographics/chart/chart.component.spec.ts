/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {ChartsModule} from "ng2-charts";

describe('ChartComponent', () => {
  let component: ChartComponent;
  let fixture: ComponentFixture<ChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChartsModule],
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
    expect(component.chartDataSet).toEqual([10, 20, 30]);
    expect(component.chartOptions).toEqual({});
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
    expect(component.chartDataSet).toEqual([10, 20, 30]);
    expect(component.chartOptions).toEqual({
      scales: {
        yAxes: [
          {
            ticks: {
              min: 0
            }
          }
        ]
      }
    });
  });

  it('should not initialize chart if chartData is not provided', () => {
    component.chartType = 'doughnut';
    component.chartLegend = true;

    component.ngOnInit();

    expect(component.chartLabels).toBeUndefined();
    expect(component.chartDataSet).toBeUndefined();
    expect(component.chartOptions).toEqual({});
  });
});

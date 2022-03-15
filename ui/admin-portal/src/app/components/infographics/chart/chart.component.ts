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

import {Component, Input, OnInit} from '@angular/core';
import {ChartOptions, ChartType} from "chart.js";
import {Label, MultiDataSet, SingleDataSet} from "ng2-charts";
import {DataRow} from "../../../model/data-row";

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements OnInit {

  @Input() chartData: DataRow[];
  @Input() chartType: ChartType = 'doughnut';
  @Input() chartLegend: boolean;

  chartLabels: Label[];
  chartOptions: ChartOptions = {};
  chartDataSet: SingleDataSet;

  constructor() { }

  ngOnInit() {
    if (this.chartData){
      const amountArray = [];
      this.chartLabels = [];
      for (let i = 0; i < this.chartData.length; i++) {
        this.chartLabels.push(this.chartData[i] ? this.chartData[i].label : '');
        amountArray.push(this.chartData[i] ? this.chartData[i].value : '');
      }
      if (this.chartType === "bar") {
        this.chartOptions = {
          scales: {
            yAxes: [
              {
                ticks: {
                  min: 0,
                }
              }
            ]
          }
        };
      }
      this.chartDataSet = amountArray;
    }

  }

}

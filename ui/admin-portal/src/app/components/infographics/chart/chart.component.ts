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

import {Component, Input, OnInit} from '@angular/core';
import {ChartData, ChartOptions, ChartType} from "chart.js";
import {DataRow} from "../../../model/data-row";

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements OnInit {

  @Input() chartData: DataRow[];
  @Input() chartType: ChartType = 'doughnut';
  @Input() chartLegend: boolean = true;

  chartLabels: string[] = [];
  chartOptions: ChartOptions = {};
  chartDataSet: ChartData;

  constructor() {
    this.chartDataSet = {
      labels: this.chartLabels,
      datasets: [
        { data: [] }
      ]
    };
  }

  ngOnInit() {
    if (this.chartData){
      const amountArray = [];
      this.chartLabels = [];
      for (let i = 0; i < this.chartData.length; i++) {
        this.chartLabels.push(this.chartData[i] ? this.chartData[i].label : '');
        amountArray.push(this.chartData[i] ? this.chartData[i].value : 0);
      }
      if (this.chartType === "bar") {
        this.chartOptions = {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true,
              min: 0
            }
          }
        };
      } else {
        this.chartOptions = {
          responsive: true
        };
      }

      this.chartDataSet = {
        labels: this.chartLabels,
        datasets: [
          { data: amountArray }
        ]
      };
    }
  }

}

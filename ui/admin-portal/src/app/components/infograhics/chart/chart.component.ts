import {Component, Input, OnInit} from '@angular/core';
import {ChartType} from "chart.js";
import {Label, MultiDataSet} from "ng2-charts";
import {DataRow} from "../../../model/data-row";

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements OnInit {

  @Input() chartData : DataRow[];

  chartLabels: Label[];
  chartMultiData: MultiDataSet;
  doughnutChartType: ChartType = 'doughnut';

  constructor() { }

  ngOnInit() {
    if (this.chartData){
      let amountArray = [];
      this.chartLabels = [];
      for (let i = 0; i < this.chartData.length; i++) {
        this.chartLabels.push(this.chartData[i] ? this.chartData[i].label : '');
        amountArray.push(this.chartData[i] ? this.chartData[i].value : '');
      }
      this.chartMultiData = [amountArray];
    }

  }

}

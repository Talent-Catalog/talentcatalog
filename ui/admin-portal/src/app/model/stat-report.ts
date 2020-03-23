import {DataRow} from "./data-row";
import {ChartType} from "chart.js";

export interface StatReport {
  name: string;
  rows: DataRow[];
  chartType: ChartType;
}

import {Component, OnInit} from '@angular/core';
import {CandidateStatService} from "../../services/candidate-stat.service";
import {StatReport} from "../../model/stat-report";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-infographic',
  templateUrl: './infographic.component.html',
  styleUrls: ['./infographic.component.scss']
})
export class InfographicComponent implements OnInit {

  loading: boolean = false;
  error: any;
  statReports: StatReport[];
  dateFilter: FormGroup;

  constructor(private statService: CandidateStatService,
              private fb: FormBuilder,) {
  }

  ngOnInit() {

    this.dateFilter = this.fb.group({
      dateFrom: ['', [Validators.required]],
      dateTo: ['', [Validators.required]]
    });

  }

  submitDate(){
    this.loading = true;

    this.statService.getAllStats(this.dateFilter.value).subscribe(result => {
        this.loading = false;
        this.statReports = result;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  exportStats() {
      let options = {type: 'text/csv;charset=utf-8;'};
      let filename = 'stats.csv';

      let csv: string[] = [];

      // Add date filter to export csv
      csv.push('"' + 'Exported Date' + '","' + new Date().toDateString() + '"\n');
      csv.push('"' + 'Date From' + '","' + this.dateFilter.value.dateFrom + '"\n')
      csv.push('"' + 'Date To' + '","' + this.dateFilter.value.dateTo + '"\n')
      csv.push('\n');

      // Add data to export csv
      for (let statReport of this.statReports) {
        csv.push(statReport.name + '\n');
        for (let row of statReport.rows) {
          csv.push('"' + row.label + '","' + row.value.toString() + '"\n')
        }
        csv.push('\n');
      }

    let blob = new Blob(csv, options);

    if (navigator.msSaveBlob) {
      // IE 10+
      navigator.msSaveBlob(blob, filename);
    } else {
      let link = document.createElement('a');
      // Browsers that support HTML5 download attribute
      if (link.download !== undefined) {
        let url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    }
  }

}

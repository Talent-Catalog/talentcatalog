import {Component, OnInit} from '@angular/core';
import {CandidateStatService} from "../../services/candidate-stat.service";
import {DataRow} from "../../model/data-row";

@Component({
  selector: 'app-infographic',
  templateUrl: './infographic.component.html',
  styleUrls: ['./infographic.component.scss']
})
export class InfographicComponent implements OnInit {

  loading: boolean = false;
  error: any;


  nationalityData: DataRow[];

  constructor(private statService: CandidateStatService) {
  }

  ngOnInit() {
    this.loading = true;
    this.statService.getNationalityData().subscribe(result => {
        this.loading = false;
        this.nationalityData = result;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    )
  }

}

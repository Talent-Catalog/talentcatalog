import {Component, OnInit} from '@angular/core';
import {CandidateService} from "../../services/candidate.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {


  constructor(private candidateService: CandidateService) {
  }

  ngOnInit() {
    this.candidateService.get
  }
}


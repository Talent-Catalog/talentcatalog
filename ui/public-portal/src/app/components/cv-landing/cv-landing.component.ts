import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {CvService} from "../../services/cv.service";
import {Candidate} from "../../model/candidate";

@Component({
  selector: 'app-cv-landing',
  templateUrl: './cv-landing.component.html',
  styleUrls: ['./cv-landing.component.scss']
})
export class CvLandingComponent implements OnInit {

  candidate: Candidate;
  error;
  loading: boolean;

  constructor(private route: ActivatedRoute, private cvService: CvService) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const token = params.get('token');
      if (token) {
        this.fetchCv(token);
      }
    });
  }

  private fetchCv(token: string) {
    this.error = null;
    this.loading = true;
    this.cvService.decodeCvRequest(token).subscribe(
      (candidate: Candidate) => {
        this.candidate = candidate;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    )
  }
}

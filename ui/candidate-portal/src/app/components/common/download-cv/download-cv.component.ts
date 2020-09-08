import {Component, OnInit} from '@angular/core';
import {CandidateService} from '../../../services/candidate.service';

@Component({
  selector: 'app-download-cv',
  templateUrl: './download-cv.component.html',
  styleUrls: ['./download-cv.component.scss']
})
export class DownloadCvComponent implements OnInit {

  error;
  loading: boolean = false;

  constructor(public candidateService: CandidateService) { }

  ngOnInit() {
  }

  downloadCV() {
    this.loading = true;
    this.candidateService.downloadCv().subscribe(
      result => {
        const tab = window.open();
        tab.location.href = URL.createObjectURL(result);
        this.loading = false
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

}

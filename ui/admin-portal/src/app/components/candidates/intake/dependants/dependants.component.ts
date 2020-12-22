import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';
import {Nationality} from '../../../../model/nationality';
import {CandidateDependantsService} from '../../../../services/candidate-citizenship.service';

@Component({
  selector: 'app-dependants',
  templateUrl: './dependants.component.html',
  styleUrls: ['./dependants.component.scss']
})
export class DependantsComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  error: boolean;
  @Input() nationalities: Nationality[];
  saving: boolean;

  constructor(
    private candidateDependantsService: CandidateDependantsService
  ) {}

  ngOnInit(): void {
  }

  addRecord() {
    this.saving = true;
    this.candidateDependantsService.create(this.candidate.id, {}).subscribe(
      (citizenship) => {
        this.candidateIntakeData.candidateDependants.push(citizenship)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  deleteRecord(i: number) {
    this.candidateIntakeData.candidateDependants.splice(i, 1);
  }

}

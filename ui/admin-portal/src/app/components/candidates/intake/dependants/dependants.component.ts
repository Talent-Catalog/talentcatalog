import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';
import {Nationality} from '../../../../model/nationality';
import {CandidateDependantService} from '../../../../services/candidate-dependant.service';

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
    private candidateDependantService: CandidateDependantService
  ) {}

  ngOnInit(): void {
  }

  addRecord() {
    this.saving = true;
    this.candidateDependantService.create(this.candidate.id, {}).subscribe(
      (dependant) => {
        this.candidateIntakeData.candidateDependants.push(dependant)
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

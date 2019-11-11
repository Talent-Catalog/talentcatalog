import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {CandidateSkill} from "../../../../model/candidate-skill";
import {CandidateSkillService} from "../../../../services/candidate-skill.service";

@Component({
  selector: 'app-view-candidate-skill',
  templateUrl: './view-candidate-skill.component.html',
  styleUrls: ['./view-candidate-skill.component.scss']
})
export class ViewCandidateSkillComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  candidateSkills: CandidateSkill[];
  loading: boolean;
  error;

  constructor(private candidateSkillService: CandidateSkillService) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }
  }

  doSearch() {
    this.loading = true;
    let request = {
      candidateId: this.candidate.id,
      pageNumber: 0,
      pageSize: 20
    };
    this.candidateSkillService.search(request).subscribe(
      candidateSkills => {
        this.candidateSkills = candidateSkills.content;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }


}

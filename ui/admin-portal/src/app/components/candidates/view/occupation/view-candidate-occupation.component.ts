import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {FormBuilder, FormGroup} from "@angular/forms";
import {Candidate} from "../../../../model/candidate";
import {CandidateOccupation} from "../../../../model/candidate-occupation";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateOccupationService} from "../../../../services/candidate-occupation.service";
import {CandidateJobExperience} from "../../../../model/candidate-job-experience";
import {CandidateJobExperienceService} from "../../../../services/candidate-job-experience.service";
import {EditCandidateJobExperienceComponent} from "./experience/edit/edit-candidate-job-experience.component";

@Component({
  selector: 'app-view-candidate-occupation',
  templateUrl: './view-candidate-occupation.component.html',
  styleUrls: ['./view-candidate-occupation.component.scss']
})
export class ViewCandidateOccupationComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  candidateJobExperienceForm: FormGroup;
  loading: boolean;
  error;
  candidateOccupation: CandidateOccupation;
  results: CandidateOccupation[];
  experiences: CandidateJobExperience[];
  orderOccupation: boolean;
  hasMore: boolean;
  sortDirection: string;

  constructor(private candidateService: CandidateService,
              private candidateOccupationService: CandidateOccupationService,
              private candidateJobExperienceService: CandidateJobExperienceService,
              private modalService: NgbModal,
              private fb: FormBuilder) { }

  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges) {
    this.experiences = [];
    this.orderOccupation = true;

    this.candidateJobExperienceForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 10,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['endDate']]
    });
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.doSearch();
    }
  }

  doSearch() {
    /* GET CANDIDATE */
    this.candidateService.get(this.candidate.id).subscribe(
      candidate => {
          this.candidate = candidate;
          this.loading = false;
        },
      error => {
          this.error = error;
          this.loading = false;
        });

    /* GET CANDIDATE OCCUPATIONS */
    this.candidateOccupationService.get(this.candidate.id).subscribe(
      results => {
         this.results = results;
         this.loading = false;
         },
      error => {
         this.error = error;
         this.loading = false;
       }
    );
    /* GET CANDIDATE EXPERIENCE */
    this.candidateJobExperienceService.search(this.candidateJobExperienceForm.value).subscribe(
      results => {
        this.experiences = results.content;
        this.hasMore = results.totalPages > results.number+1;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;

  }

  editCandidateJobExperience(candidateJobExperience: CandidateJobExperience) {
    const editCandidateJobExperienceModal = this.modalService.open(EditCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateJobExperienceModal.componentInstance.candidateJobExperience = candidateJobExperience;

    editCandidateJobExperienceModal.result
      .then((candidateJobExperience) => this.doSearch())
      .catch(() => { /* Isn't possible */
      });

  }
}


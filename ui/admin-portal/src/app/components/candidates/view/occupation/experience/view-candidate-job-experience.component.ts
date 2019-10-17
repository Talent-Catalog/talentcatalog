import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../../model/candidate";
import {Occupation} from "../../../../../model/occupation";
import {CandidateOccupation} from "../../../../../model/candidate-occupation";
import {CandidateJobExperience} from "../../../../../model/candidate-job-experience";
import {CandidateJobExperienceService} from "../../../../../services/candidate-job-experience.service";
import {EditCandidateJobExperienceComponent} from "./edit/edit-candidate-job-experience.component";
import {CreateCandidateJobExperienceComponent} from "./create/create-candidate-job-experience.component";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SearchResults} from "../../../../../model/search-results";

@Component({
  selector: 'app-view-candidate-job-experience',
  templateUrl: './view-candidate-job-experience.component.html',
  styleUrls: ['./view-candidate-job-experience.component.scss']
})
export class ViewCandidateJobExperienceComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() candidateOccupation: CandidateOccupation;

  candidateJobExperienceForm: FormGroup;
  loading: boolean;
  expanded: boolean;
  error;
  results: SearchResults<CandidateJobExperience>;
  experiences: CandidateJobExperience[];
  hasMore: boolean;

  constructor(private candidateJobExperienceService: CandidateJobExperienceService,
              private modalService: NgbModal,
              private fb: FormBuilder) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.editable = true;
    this.expanded = false;
    this.experiences = [];

    this.candidateJobExperienceForm = this.fb.group({
      candidateOccupationId: [this.candidateOccupation.id],
      pageSize: 1,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['endDate']]
    });

    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }

  }

  doSearch() {
    this.loading = true;
    this.candidateJobExperienceService.search(this.candidateJobExperienceForm.value).subscribe(
      results => {
        this.experiences.push(...results.content);
        this.hasMore = results.totalPages > results.number+1;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;

  }

  loadMore() {
   this.candidateJobExperienceForm.controls['pageNumber'].patchValue(this.candidateJobExperienceForm.value.pageNumber+1);
   this.doSearch();
  }

  createCandidateJobExperience() {
    const createCandidateJobExperienceModal = this.modalService.open(CreateCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateJobExperienceModal.componentInstance.candidateOccupationId = this.candidateOccupation.id;
    createCandidateJobExperienceModal.componentInstance.candidateId = this.candidate.id;

    createCandidateJobExperienceModal.result
      .then((candidateJobExperience) => this.doSearch())
      .catch(() => { /* Isn't possible */
      });

  }

  editCandidateJobExperience(candidateJobExperience: CandidateJobExperience) {
    const editCandidateJobExperienceModal = this.modalService.open(EditCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateJobExperienceModal.componentInstance.candidateJobExperience = candidateJobExperience;
    editCandidateJobExperienceModal.componentInstance.candidateOccupationId = this.candidateOccupation.id;

    editCandidateJobExperienceModal.result
      .then((candidateJobExperience) => this.doSearch())
      .catch(() => { /* Isn't possible */
      });

  }

}

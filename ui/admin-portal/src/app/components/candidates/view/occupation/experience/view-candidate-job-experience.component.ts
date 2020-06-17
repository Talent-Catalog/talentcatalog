import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../../model/candidate';
import {CandidateOccupation} from '../../../../../model/candidate-occupation';
import {CandidateJobExperience} from '../../../../../model/candidate-job-experience';
import {CandidateJobExperienceService} from '../../../../../services/candidate-job-experience.service';
import {EditCandidateJobExperienceComponent} from './edit/edit-candidate-job-experience.component';
import {CreateCandidateJobExperienceComponent} from './create/create-candidate-job-experience.component';
import {FormBuilder, FormGroup} from '@angular/forms';
import {SearchResults} from '../../../../../model/search-results';
import {EditCandidateOccupationComponent} from '../edit/edit-candidate-occupation.component';
import {SavedSearch} from "../../../../../model/saved-search";
import {DeleteCandidateOccupationComponent} from "../delete/delete-candidate-occupation.component";

@Component({
  selector: 'app-view-candidate-job-experience',
  templateUrl: './view-candidate-job-experience.component.html',
  styleUrls: ['./view-candidate-job-experience.component.scss']
})
export class ViewCandidateJobExperienceComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() candidateOccupation: CandidateOccupation;
  @Output() deleteOccupation = new EventEmitter<CandidateOccupation>();

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
    this.expanded = false;
    this.experiences = [];

    this.candidateJobExperienceForm = this.fb.group({
      candidateOccupationId: [this.candidateOccupation.id],
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
    this.loading = true;
    this.experiences = [];

    /* GET CANDIDATE JOB EXPERIENCES */
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

  loadMore() {
   this.candidateJobExperienceForm.controls['pageNumber'].patchValue(this.candidateJobExperienceForm.value.pageNumber+1);
   this.doSearch();
  }

  verifyOccupation() {
    const modal = this.modalService.open(EditCandidateOccupationComponent, {
      centered: true,
      backdrop: 'static'
    });

    modal.componentInstance.candidateOccupation = this.candidateOccupation;

    modal.result
      .then((candidateOccupation) => this.candidateOccupation = candidateOccupation)
      .catch(() => { /* Isn't possible */
      });

    this.candidateJobExperienceForm.controls['candidateOccupationId'].patchValue(this.candidateOccupation.id);
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

    editCandidateJobExperienceModal.result
      .then((candidateJobExperience) => this.doSearch())
      .catch(() => { /* Isn't possible */
      });

  }

  doDeleteCandidateOccupation() {
    // get occupation
    this.candidateOccupation.occupation.id
    // check occupation doesn't have work experience
    if(this.experiences.length == 0) {
        this.deleteOccupation.emit(this.candidateOccupation);
    } else {
      this.deleteModal()
    }
    // throw modal

    // delete occupation
  }

  deleteModal() {
    const deleteCandidateOccupationModal = this.modalService.open(DeleteCandidateOccupationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCandidateOccupationModal.result
      .then(() => this.deleteOccupation.emit(this.candidateOccupation))
      .catch(() => { /* Isn't possible */
      });

  }

}

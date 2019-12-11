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
  _loading = {
    experience: true,
    occupation: true,
    candidate: true
  };
  error;
  candidateOccupations: CandidateOccupation[];
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
      sortFields: [['startDate']]
    });
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }
  }

  get loading() {
    const l = this._loading;
    return l.experience || l.occupation || l.candidate;
  }

  doSearch() {
    /* GET CANDIDATE */
    this.candidateService.get(this.candidate.id).subscribe(
      candidate => {
          this.candidate = candidate;
          this._loading.candidate = false;
        },
      error => {
          this.error = error;
          this._loading.candidate = false;
        });

    /* GET CANDIDATE OCCUPATIONS */
    this.candidateOccupationService.get(this.candidate.id).subscribe(
      results => {
         this.candidateOccupations = results;
         this._loading.occupation = false;
         },
      error => {
         this.error = error;
         this._loading.occupation = false;
       }
    );

    this.loadJobExperiences();
  }

  editCandidateJobExperience(candidateJobExperience: CandidateJobExperience) {
    const editCandidateJobExperienceModal = this.modalService.open(EditCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateJobExperienceModal.componentInstance.candidateJobExperience = candidateJobExperience;

    editCandidateJobExperienceModal.result
      .then(() => this.doSearch())
      .catch(() => { /* Isn't possible */
      });
  }

  loadJobExperiences(more: boolean = false) {
    if (more) {
      // Load the next page
      const page = this.candidateJobExperienceForm.value.pageNumber;
      this.candidateJobExperienceForm.patchValue({pageNumber: page + 1});
    } else {
      // Load the first page
      this.candidateJobExperienceForm.patchValue({pageNumber: 0});
    }

    /* GET CANDIDATE EXPERIENCE */
    this.candidateJobExperienceService.search(this.candidateJobExperienceForm.value).subscribe(
      results => {
        if (more) {
          this.experiences = this.experiences.concat(results.content);
        } else {
          this.experiences = results.content;
        }
        this.hasMore = results.totalPages > results.number + 1;
        this._loading.experience = false;
      },
      error => {
        this.error = error;
        this._loading.experience = false;
      });
  }
}


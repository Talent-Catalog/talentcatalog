import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {Candidate} from "../../../../../model/candidate";
import {SurveyType} from "../../../../../model/survey-type";
import {SurveyTypeService} from "../../../../../services/survey-type.service";

@Component({
  selector: 'app-edit-candidate-survey',
  templateUrl: './edit-candidate-survey.component.html',
  styleUrls: ['./edit-candidate-survey.component.scss']
})
export class EditCandidateSurveyComponent implements OnInit {

  candidateId: number;

  candidateForm: FormGroup;

  error;
  loading: boolean;
  saving: boolean;

  surveyTypes: SurveyType[];

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private surveyTypeService: SurveyTypeService) {}

  ngOnInit() {
    this.loading = true;
    this.loadDropDownData();

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        surveyTypeId: [candidate.surveyType.id],
        surveyComment: [candidate.surveyComment]
      });
      this.loading = false;
    });
  }

  loadDropDownData() {
    /* Load the survey types  */
    this.surveyTypeService.listSurveyTypes().subscribe(
      (response) => {
        this.surveyTypes = response
          .sort((a, b) => a.id > b.id ? 1 : -1) // Order by surveyType id
      },
      (error) => {
        this.error = error;
      }
    );
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateSurvey(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.closeModal(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}

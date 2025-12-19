/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output
} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {CandidateExamService} from '../../../services/candidate-exam.service';
import {CandidateExam} from '../../../model/candidate';
import {RegistrationService} from "../../../services/registration.service";
import {CandidateService} from "../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DeleteExamComponent} from "./delete/delete-exam.component";

@Component({
  selector: 'app-registration-candidate-exam',
  templateUrl: './registration-candidate-exam.component.html',
  styleUrls: ['./registration-candidate-exam.component.scss']
})
export class RegistrationCandidateExamComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  error: any;
  loading: boolean;
  saving: boolean;

  form: UntypedFormGroup;
  candidateExams: CandidateExam[];
  addingExam: boolean;


  editTarget: CandidateExam;
  subscription;
  years: number[];


  constructor(private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private candidateExamService: CandidateExamService,
              public registrationService: RegistrationService,
              private modalService: NgbModal) { }


  ngOnInit() {
    this.candidateExams = [];
    this.saving = false;
    this.loading = false;
    this.clearForm();

    /* Load the candidate data */
    this.loadCandidateExams();
  }

  loadCandidateExams() {
    this.candidateService.getCandidateCandidateExams().subscribe(
      (candidate) => {
        this.candidateExams = candidate.candidateExams || [];
        this.addingExam = !this.candidateExams.length;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }
  deleteExam(exam: CandidateExam, index: number) {
    const deleteExamModal = this.modalService.open(DeleteExamComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteExamModal.result
    .then((result) => {
      if (result === true) {
        this.saving = true;
        this.candidateExamService.deleteCandidateExam(exam.id).subscribe(
          () => {
            this.candidateExams = this.candidateExams.filter((_, i) => i !== index);
            this.addingExam = !this.candidateExams.length;
            this.saving = false;
            this.loadCandidateExams();
          },
          (error) => {
            this.error = error;
            this.saving = false;
          }
        );
      }
    })
    .catch(() => { /* Handle modal dismissal without deletion */ });
  }

  next() {
    this.onSave.emit();
    this.registrationService.next();
  }

  back() {
    this.registrationService.back();
  }

  finishEditing() {
    this.onSave.emit();
  }

  handleCandidateExamCreated(exam: CandidateExam) {
    let index = -1;
    if (this.candidateExams.length) {
      index = this.candidateExams.findIndex(ex => ex.id === exam.id);
    }
    /* Replace the old exam item with the updated item */
    if (index >= 0) {
      this.candidateExams.splice(index, 1, exam);
    } else {
      this.candidateExams.push(exam);
    }
    this.addingExam = false;
  }

  editCandidateExam(exam: CandidateExam) {
    this.editTarget = exam;
  }

  handleExamSaved(exams: CandidateExam, i) {
    this.candidateExams[i] = exams;
    this.editTarget = null;
  }

  clearForm() {
    this.form = this.fb.group({
      exam: ['', Validators.required],
      otherExam: ['', Validators.required],
      score: ['', Validators.required],
      year: ['', Validators.required],
      notes: ['', Validators.required],
    })
  }
}

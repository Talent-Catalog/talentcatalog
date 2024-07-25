import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CandidateExamService} from '../../../services/candidate-exam.service';
import {CandidateExam, Exam} from '../../../model/candidate';
import {TranslateService} from '@ngx-translate/core';
import {Subscription} from "rxjs";
import {RegistrationService} from "../../../services/registration.service";
import {CandidateService} from "../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DeleteExamComponent} from "./delete/delete-exam.component";
import {generateYearArray} from "../../../util/year-helper";
@Component({
  selector: 'app-registration-candidate-exam',
  templateUrl: './registration-candidate-exam.component.html',
  styleUrls: ['./registration-candidate-exam.component.scss']
})
export class RegistrationCandidateExamComponent implements OnInit, OnDestroy {

  @Input() edit: boolean = false;
  @Output() onSave = new EventEmitter<void>();
  @Input() candidateExam: CandidateExam;
  @Input() disabled: boolean;
  @Output() delete = new EventEmitter<void>();

  form: FormGroup;
  candidateExams: CandidateExam [] = [];
  showForm: boolean = true;
  error: any;
  saving: boolean = false;
  subscription: Subscription;
  invalidExam: CandidateExam | null = null;
  examListEnum: { key: string, value: string }[] = [];
  showOtherExamInput: boolean = false;
  years: number[];
  _loading = {
    candidate: true,
    exams: true
  };
  constructor(
    private fb: FormBuilder,
    private candidateExamService: CandidateExamService,
    public translateService: TranslateService,
    public registrationService: RegistrationService,
    private candidateService: CandidateService,
    private modalService: NgbModal
  ) { }

  ngOnInit() {
    this.setUpForm();
    this.loadDropDownData();
    this.subscription = this.translateService.onLangChange.subscribe(() => {
      this.loadDropDownData();
    });
    this.years = generateYearArray(1950,true);
    this.candidateService.getCandidateCandidateExams().subscribe(
      (candidate) => {
        console.log(candidate)
        this.candidateExams = candidate.candidateExams.map(ce => {
          return {
            id: ce.id,
            exam: ce.exam,
            otherExam: ce.otherExam,
            year: ce.year,
            score: ce.score,
            notes: ce.notes,
          };
        });
        this._loading.candidate = false;
        this.showForm = this.candidateExams.length === 0;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  loadDropDownData() {
    this.examListEnum = Object.keys(Exam).map(key => ({ key, value: Exam[key] }));
    this._loading.exams = false;
  }

  setUpForm() {
    this.form = this.fb.group({
      exam: [null, Validators.required],
      otherExam: [null],
      score: [null, Validators.required],
      year: [null, [Validators.required, Validators.min(1950)]],
      notes: [null]
    });
    // Watch for changes in examId to toggle "other" input
    this.form.get('exam')?.valueChanges.subscribe(value => {
      this.showOtherExamInput = value === 'Other';
    });
  }

  addExam() {
    if (this.form.valid) {
      this.candidateExams.push(this.form.value);
      this.showOtherExamInput = false;
      this.form.reset();
    }
    this.showForm = true;
  }
  save(dir: string) {
    if (this.form.valid) {
      this.addExam();
    }
    this.invalidExam = this.candidateExams.find(candidateExam => candidateExam.year < 1950 || candidateExam.year == null);
    const request = {
      updates: this.candidateExams
    };
    if (!this.invalidExam) {
      this.candidateExamService.updateCandidateExams(request).subscribe(
        (response) => {
          if (dir === 'next') {
            this.onSave.emit();
            this.registrationService.next();
          } else {
            this.registrationService.back();
          }
        },
        (error) => {
          this.error = error;
        });
    } else {
      this.error = "You need to put in a years value (from 1950 upwards).";
    }
  }
  get loading() {
    return this._loading.candidate || this._loading.exams;
  }
  cancel() {
    this.onSave.emit();
  }

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
  }
  deleteCandidateExam(index: number,examId:number) {
    this.deleteModal(index);
  }
  deleteModal(index: number) {
    const deleteExamModal = this.modalService.open(DeleteExamComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteExamModal.result
    .then((result) => {
      // remove exam from candidateExams if confirmed modal
      if (result === true) {
        this.candidateExams.splice(index, 1);
      }
    })
    .catch(() => { /* Isn't possible */ });
  }
  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}

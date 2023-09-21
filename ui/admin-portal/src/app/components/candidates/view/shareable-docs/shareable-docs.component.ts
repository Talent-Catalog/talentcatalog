import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {AutoSaveComponentBase} from "../../../util/autosave/AutoSaveComponentBase";
import {Candidate, UpdateCandidateShareableDocsRequest} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {Observable} from "rxjs";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {CandidateService} from "../../../../services/candidate.service";
import {isSavedList} from "../../../../model/saved-list";
import {CandidateSource} from "../../../../model/base";

@Component({
  selector: 'app-shareable-docs',
  templateUrl: './shareable-docs.component.html',
  styleUrls: ['./shareable-docs.component.scss']
})
export class ShareableDocsComponent extends AutoSaveComponentBase implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() candidateSource: CandidateSource;

  @Output() updatedShareableCV = new EventEmitter<CandidateAttachment>();

  cvs: CandidateAttachment[];
  other: CandidateAttachment[];

  savedList: boolean;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService) {
    super(candidateService);
  }

  ngOnInit() {
    this.cvs = this.candidate.candidateAttachments?.filter(a => a.cv === true);
    this.other = this.candidate.candidateAttachments?.filter(a => a.cv === false);

    // Only lists have listShareable attachments
    if (this.isList) {
      this.form = this.fb.group({
        shareableCvAttachmentId: [this.candidate?.listShareableCv?.id],
        shareableDocAttachmentId: [this.candidate?.listShareableDoc?.id],
      });
    } else {
      this.form = this.fb.group({
        shareableCvAttachmentId: [this.candidate?.shareableCv?.id],
        shareableDocAttachmentId: [this.candidate?.shareableDoc?.id],
      });
    }

  }

  doSave(formValue: any): Observable<Candidate> {
    const request: UpdateCandidateShareableDocsRequest = {
      shareableCvAttachmentId: formValue.shareableCvAttachmentId,
      shareableDocAttachmentId: formValue.shareableDocAttachmentId
    }
    if (this.isList) {
      request.savedListId = this.candidateSource.id;
    }
    return this.candidateService.updateShareableDocs(this.candidate.id, request);
  }

  onSuccessfulSave() {
    /**
     * Update the candidate object once saved.
     * If it's a list update the list shareable document otherwise update the default shareable object.
     */
    if (this.isList) {
      if (this.shareableCvId != null) {
        this.candidate.listShareableCv = this.cvs.find(att => att.id === this.shareableCvId);
      } else {
        this.candidate.listShareableCv = null;
      }
      if (this.shareableDocId != null) {
        this.candidate.listShareableDoc = this.other.find(att => att.id === this.shareableDocId);
      } else {
        this.candidate.listShareableDoc = null;
      }
      this.updatedShareableCV.emit(this.candidate.listShareableCv);
    } else {
      if (this.shareableCvId != null) {
        this.candidate.shareableCv = this.cvs.find(att => att.id === this.shareableCvId);
      } else {
        this.candidate.shareableCv = null;
      }
      if (this.shareableDocId != null) {
        this.candidate.shareableDoc = this.other.find(att => att.id === this.shareableDocId);
      } else {
        this.candidate.shareableDoc = null;
      }
      this.updatedShareableCV.emit(this.candidate.shareableCv);
    }

  }

  get shareableCvId() {
    return this.form.value?.shareableCvAttachmentId;
  }

  get shareableDocId() {
    return this.form.value?.shareableDocAttachmentId;
  }

  get isList() {
    return isSavedList(this.candidateSource);
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Replace the form value with the new candidates context notes when
    //changing from one candidate to the next or when selection has changed.
    this.cvs = this.candidate.candidateAttachments?.filter(a => a.cv === true);
    this.other = this.candidate.candidateAttachments?.filter(a => a.cv === false);
    if (this.form) {
      if (this.isList) {
        this.form.controls['shareableCvAttachmentId'].patchValue(this.candidate?.listShareableCv?.id);
        this.form.controls['shareableDocAttachmentId'].patchValue(this.candidate?.listShareableDoc?.id);
      } else {
        this.form.controls['shareableCvAttachmentId'].patchValue(this.candidate?.shareableCv?.id);
        this.form.controls['shareableDocAttachmentId'].patchValue(this.candidate?.shareableDoc?.id);
      }
    }
  }

}

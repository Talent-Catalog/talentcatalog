import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
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

  @Input() cvs: CandidateAttachment[];
  @Input() other: CandidateAttachment[];

  savedList: boolean;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService) {
    super();
  }

  ngOnInit() {
    // if is saved search if is saved list (remove source type)
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
    // How to avoid loosing the value in the front end when changing? Have only the ID not the full value so can't set
    // with form value.
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

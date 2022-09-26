import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedListService} from "../../../../services/saved-list.service";
import {SavedList, SearchSavedListRequest} from "../../../../model/saved-list";
import {environment} from "../../../../../environments/environment";

@Component({
  selector: 'app-create-external-link',
  templateUrl: './create-external-link.component.html',
  styleUrls: ['./create-external-link.component.scss']
})
export class CreateExternalLinkComponent implements OnInit {

  form: UntypedFormGroup;
  error;
  saving: boolean;
  savedLists: SavedList[];
  loading: boolean;
  publishUrl: string = environment.publishUrl;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private savedListService: SavedListService) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      savedListId: [null, Validators.required],
      tbbShortName: [null, Validators.required],
    });
    const request: SearchSavedListRequest = {
      owned: true,
      shared: true,
      global: true,
      shortName: false
    };
    this.savedListService.search(request).subscribe(
      (response) => {
        this.savedLists = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  onSave() {
    this.saving = true;
    this.savedListService.updateShortName(this.form.value).subscribe(
      (link) => {
        this.closeModal(link)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(link: SavedList) {
    this.activeModal.close(link);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}

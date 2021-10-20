import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedListLinkService} from "../../../../services/saved-list-link.service";
import {SavedListLink} from "../../../../model/saved-list-link";
import {SavedList, SearchSavedListRequest} from "../../../../model/saved-list";
import {SavedListService} from "../../../../services/saved-list.service";

@Component({
  selector: 'app-edit-external-link',
  templateUrl: './edit-external-link.component.html',
  styleUrls: ['./edit-external-link.component.scss']
})
export class EditExternalLinkComponent implements OnInit {

  linkForm: FormGroup;
  savedListLink: SavedListLink;
  error;
  saving: boolean;
  savedLists: SavedList[];
  loading: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedListLinkService: SavedListLinkService,
              private savedListService: SavedListService) {
  }

  ngOnInit() {
    this.linkForm = this.fb.group({
      savedListId: [this.savedListLink.savedList?.id, Validators.required],
      link: [this.savedListLink?.link, Validators.required],
    });
    const request: SearchSavedListRequest = {

    }
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
    this.savedListLinkService.update(this.savedListLink.id, this.linkForm.value).subscribe(
      (link) => {
        this.closeModal(link)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(link: SavedListLink) {
    this.activeModal.close(link);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}

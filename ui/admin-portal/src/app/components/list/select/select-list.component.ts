import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {FormBuilder, FormGroup} from '@angular/forms';
import {SavedList, SearchSavedListRequest} from '../../../model/saved-list';
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {SavedListService} from '../../../services/saved-list.service';
import {JoblinkValidationEvent} from '../../util/joblink/joblink.component';


export interface TargetListSelection {
  //List id - 0 if new list requested
  savedListId: number;

  //Name of new list to be created (if any - only used if savedListId = 0)
  newListName?: string;

  //If true any existing contents of target list should be replaced, otherwise
  //contents are added (merged).
  replace: boolean;

  sfJoblink?: string;
}


@Component({
  selector: 'app-select-list',
  templateUrl: './select-list.component.html',
  styleUrls: ['./select-list.component.scss']
})
export class SelectListComponent implements OnInit {

  error: string = null;
  excludeList: SavedList;
  form: FormGroup;
  jobName: string;
  loading: boolean;
  saving: boolean;
  sfJoblink: string;
  action: string = "Save";
  title: string = "Select List";

  lists: SavedList[] = [];

  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    enableCheckAll: false,
    singleSelection: true,
    allowSearchFilter: true
  };

  constructor(
    private savedListService: SavedListService,
    private activeModal: NgbActiveModal,
    private fb: FormBuilder) { }

  ngOnInit() {
    this.form = this.fb.group({
      newListName: [null],
      newList: [false],
      savedList: [null],
      replace: [false],
    });
    this.loadLists();
  }

  get newListNameControl() { return this.form.get('newListName'); }
  get newListName(): string { return this.form.value.newListName; }
  get newList(): boolean { return this.form.value.newList; }
  get replace(): boolean { return this.form.value.replace; }
  get savedList(): SavedList { return this.form.value.savedList; }

  private loadLists() {
    /*load all our non fixed lists */
    this.loading = true;
    const request: SearchSavedListRequest = {
      owned: true,
      shared: true,
      fixed: false
    };

    this.savedListService.search(request).subscribe(
      (results) => {
        this.lists = results.filter(list => list.id !== this.excludeList?.id) ;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  select() {
    const selection: TargetListSelection = {
      savedListId: this.savedList === null ? 0 : this.savedList[0].id,
      newListName: this.newList ? this.newListName : null,
      replace: this.replace,
      sfJoblink: this.sfJoblink ? this.sfJoblink : null
    }
    this.activeModal.close(selection);
  }

  disableNew() {
    this.form.controls['newList'].disable();
  }

  enableNew() {
    this.form.controls['newList'].enable();
    this.form.controls['savedList'].patchValue(null);
  }


  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;

      //If existing name is empty, auto copy into them
      if (!this.newListNameControl.value) {
        this.newListNameControl.patchValue(this.jobName);
      }
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }
}

import {Component, OnInit} from '@angular/core';
import {SavedList} from "../../../model/saved-list";
import {Location} from "@angular/common"
import {ActivatedRoute, Router} from "@angular/router";
import {SavedListService} from "../../../services/saved-list.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {getCandidateSourceNavigation} from "../../../model/saved-search";
import {UpdateListComponent} from "../../list/update/update-list.component";

@Component({
  selector: 'app-candidates-list',
  templateUrl: './candidates-list.component.html',
  styleUrls: ['./candidates-list.component.scss']
})
export class CandidatesListComponent implements OnInit {
  error: string;
  loading: boolean;
  pageNumber: number;
  pageSize: number;
  savedList: SavedList;
  private id: number;

  constructor(private route: ActivatedRoute,
              private modalService: NgbModal,
              private location: Location,
              private router: Router,
              private savedListService: SavedListService) { }

  ngOnInit() {
    this.loading = true;

    // start listening to route params after everything is loaded
    this.route.queryParamMap.subscribe(
      params => {
        this.pageNumber = +params.get('pageNumber');
        if (!this.pageNumber) {
          this.pageNumber = 1;
        }
        this.pageSize = +params.get('pageSize');
        if (!this.pageSize) {
          this.pageSize = 20;
        }
      }
    );

    this.route.paramMap.subscribe(params => {
      this.id = +params.get('id');
      if (this.id) {

        //Load saved list to get name and type to display
        this.savedListService.get(this.id).subscribe(result => {
          this.savedList = result;
          this.loading = false;
        }, err => {
          this.error = err;
          this.loading = false;
        });
      } else {
        //This is the list url with no id specified.
        this.createNewList();
        this.loading = false;
      }
    });
  }

  private createNewList() {
    const modal = this.modalService.open(UpdateListComponent);
    modal.result
      .then((savedList: SavedList) => {
        const urlCommands = getCandidateSourceNavigation(savedList);
        this.router.navigate(urlCommands);
      })
      .catch(() => {
        this.location.back();
      });
  }
}

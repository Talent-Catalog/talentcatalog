import {Component, OnInit} from '@angular/core';
import {DragulaService} from "ng2-dragula";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-candidate-column-selector',
  templateUrl: './candidate-column-selector.component.html',
  styleUrls: ['./candidate-column-selector.component.scss']
})
export class CandidateColumnSelectorComponent implements OnInit {

  availableFields = [
    {name: "field2"},
    {name: "field3"},
    {name: "field4"},
  ];

  selectedFields = [
    {name: "field1"},
  ];

  constructor(private dragulaService: DragulaService,
              private activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    this.dragulaService.createGroup("FIELDS", {});

    this.dragulaService.dropModel("FIELDS").subscribe(
      args => console.log(args)
    );

  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    this.activeModal.close(true);

  }
}

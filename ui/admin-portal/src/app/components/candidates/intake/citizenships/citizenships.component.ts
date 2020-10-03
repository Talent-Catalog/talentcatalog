import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-citizenships',
  templateUrl: './citizenships.component.html',
  styleUrls: ['./citizenships.component.scss']
})
export class CitizenshipsComponent extends IntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      citizenCountry: [],
      citizenHasPassport: [],
      citizenNotes: [],
    });
  }

}

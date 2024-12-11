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

import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {SearchOppsBy} from "../../../model/base";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {CandidateOppsComponent} from "../candidate-opps/candidate-opps.component";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'app-candidate-opps-with-detail',
  templateUrl: './candidate-opps-with-detail.component.html',
  styleUrls: ['./candidate-opps-with-detail.component.scss']
})
export class CandidateOppsWithDetailComponent extends MainSidePanelBase implements OnInit {
  /**
   * Only one of these inputs should be selected - one does a search, the other just takes
   * an array of opps (for example the array of opps associated with a candidate)
   */
  @Input() searchBy: SearchOppsBy;
  @Input() candidateOpps: CandidateOpportunity[];

  /**
   * This is passed in from a higher level component which tracks whether the overall read status
   * of all the chats that it manages.
   * That component is the cases tab in the Jobs home component - which displays an asterisk
   * if some chats are unread.
   * <p/>
   * This component can call next on this subject if it knows that some of the chats it manages
   * are unread. The fact that it is a BehaviorSubject means that you can query the current status
   * of the higher level component.
   */
  @Input() chatsRead$: BehaviorSubject<boolean>;

  @Output() candidateOppUpdated = new EventEmitter<CandidateOpportunity>();

  //Pick up reference to child CandidateOppsComponent - so we can call methods on it - see below
  @ViewChild(CandidateOppsComponent, { static: false }) candidateOppsComponent: CandidateOppsComponent;

  error: any;
  loading: boolean;

  selectedOpp: CandidateOpportunity;

  constructor() {
    super(6);
  }

  ngOnInit(): void {
  }

  onOppSelected(opp: CandidateOpportunity) {
    this.selectedOpp = opp;
  }

  /**
   * The selected component which has been displayed (by ViewCandidateOppComponent) has fired
   * an event saying that the opp has been updated.
   * @param opp Modified opp.
   */
  onCandidateOppUpdated(opp: CandidateOpportunity) {
    //Was the component passed candidate opps through the @Input candidateOpps from another
    //component
    if (this.candidateOpps) {
      //Yes - pass up the updated event so that the source of the opps can update this one.
      this.candidateOppUpdated.emit(opp);
    } else if (this.searchBy) {
      //The component was passed SearchBy which will have requested its child component to
      //perform a search.
      //Ask the child component which displays all opps to refresh by searching again.
      //This will pick up the update opp - and any other updates.
      //MODEL: How to access methods on a child component. We have used @ViewChild - see above.
      //See alternatives here:
      // https://stackoverflow.com/questions/58710341/how-to-reload-or-refresh-only-child-component-in-angular-8
      this.candidateOppsComponent.search();
    }
  }
}

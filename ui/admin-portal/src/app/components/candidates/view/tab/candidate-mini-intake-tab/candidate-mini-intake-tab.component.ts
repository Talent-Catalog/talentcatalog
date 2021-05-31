/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Component, QueryList, ViewChildren} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {Subject} from "rxjs";
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-candidate-mini-intake-tab',
  templateUrl: './candidate-mini-intake-tab.component.html',
  styleUrls: ['./candidate-mini-intake-tab.component.scss']
})
export class CandidateMiniIntakeTabComponent extends IntakeComponentTabBase {
  toggleAll: Subject<any> = new Subject();
  activeIds: string[] = ['intake-confirm', 'intake-int-recruit', 'intake-destinations', 'intake-personal-status',
    'intake-english-assessment', 'intake-registration']

  @ViewChildren(NgbAccordion) accs: QueryList<NgbAccordion>;

  togglePanels(openAll: boolean) {
    this.toggleAll.next(openAll);
    if (openAll) {
      this.accs.forEach(acc => {
        acc.expandAll();
      })
    } else {
      this.accs.forEach(acc => {
        acc.collapseAll();
      })
    }
  }
}

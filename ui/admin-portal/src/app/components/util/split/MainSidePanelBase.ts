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

import {Directive} from "@angular/core";

/**
 * Use to provide support for components containing two panels, a main panel (on the left) and
 * a side panel (on the right).
 * <p/>
 * The panels can have one of two sizes - one where the side panel is minimized and one where it is
 * maximized. The main panel takes up the reset of the space.
 * <p/>
 * The html will look something like below. Note that it is driven by the following fields
 * which are inherited from this class:
 *    canToggleSizes
 *    mainPanelColWidth
 *    sidePanelColWidth
 *    sidePanelIsMax
 *
  <div class="row">
    <div class="col-sm-{{mainPanelColWidth}}">

      === Main panel content goes here ===

    </div>

    <div class="col-sm-{{sidePanelColWidth}} admin-panel">
      <div class="w-100">
        <div *ngIf="canToggleSizes()" class="float-right">
         <button class="btn btn-sm btn-outline-secondary" (click)="resizeSidePanel()"><i
            class="fas fa-arrow-{{sidePanelIsMax ? 'right' : 'left'}}"></i></button>
        </div>

        == Side panel content goes here ===

      </div>
    </div>
  </div>

 */
@Directive()
export abstract class MainSidePanelBase {

  sidePanelColWidth;
  mainPanelColWidth;

  /**
   * Configures the panels.
   * @param minSidePanelWidth Minimum size of the side panel in columns
   * @param maxSidePanelWidth Maximum size of the side panel in columns - 0 if max is same as min
   * @param sidePanelIsMax True (default) if the side panel should start at maximum width
   * @param totalPanelWidth Total number of columns - defaults to 12 (standard Bootstrap number)
   * @protected
   */
  protected constructor(private minSidePanelWidth: number, private maxSidePanelWidth = 0,
                        public sidePanelIsMax = true,
                        public totalPanelWidth = 12) {
    if (this.maxSidePanelWidth === 0) {
      this.maxSidePanelWidth = this.minSidePanelWidth;
    }
    this.sidePanelColWidth = this.sidePanelIsMax ? this.maxSidePanelWidth : this.minSidePanelWidth;
    this.mainPanelColWidth = this.totalPanelWidth - this.sidePanelColWidth;
  }

  canToggleSizes(): boolean {
      return this.maxSidePanelWidth !== this.minSidePanelWidth;
  }

  resizeSidePanel() {
    this.sidePanelIsMax = !this.sidePanelIsMax;
    this.sidePanelColWidth = this.sidePanelIsMax ? this.maxSidePanelWidth : this.minSidePanelWidth;
    this.mainPanelColWidth = this.totalPanelWidth - this.sidePanelColWidth;
  }

}

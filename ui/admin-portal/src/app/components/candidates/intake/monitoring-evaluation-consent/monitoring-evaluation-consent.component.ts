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

import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {AuthorizationService} from '../../../../services/authorization.service';

@Component({
  selector: 'app-monitoring-evaluation-consent',
  templateUrl: './monitoring-evaluation-consent.component.html',
  styleUrls: ['./monitoring-evaluation-consent.component.scss']
})
export class MonitoringEvaluationConsentComponent extends IntakeComponentBase implements OnInit {
  loading: boolean;
  public consentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, private candidateService: CandidateService,
              private authService: AuthorizationService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      monitoringEvaluationConsent: [{value: this.candidateIntakeData?.monitoringEvaluationConsent, disabled: !this.editable}],
    });
  }

  createUpdateSalesforce() {
    this.error = null;
    this.loading = true;
    this.candidateService.createUpdateLiveCandidate(this.candidate.id).subscribe(
      candidate => {
        this.candidateService.updateCandidate();
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

}

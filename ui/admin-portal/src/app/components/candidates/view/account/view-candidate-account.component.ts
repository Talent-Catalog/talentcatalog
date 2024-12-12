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

import {Component, Input, OnInit} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../model/candidate';
import {User} from '../../../../model/user';
import {CandidateService} from '../../../../services/candidate.service';
import {UserService} from '../../../../services/user.service';
import {ChangePasswordComponent} from '../../../account/change-password/change-password.component';

@Component({
  selector: 'app-view-candidate-account',
  templateUrl: './view-candidate-account.component.html',
  styleUrls: ['./view-candidate-account.component.scss']
})
export class ViewCandidateAccountComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  user: User;
  loading: boolean;
  error;

  constructor(private candidateService: CandidateService,
              private userService: UserService,
              private modalService: NgbModal) { }

  ngOnInit() {

  }

  updatePassword(user: User) {
    const updatePasswordModal = this.modalService.open(ChangePasswordComponent, {
      centered: true,
      backdrop: 'static'
    });

    updatePasswordModal.componentInstance.user = user;

    updatePasswordModal.result
      .then((user) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

}

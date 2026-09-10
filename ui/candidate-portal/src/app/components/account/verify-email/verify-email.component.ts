/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {Component, Input} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {SendVerifyEmailRequest} from '../../../model/user';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss'],
})
export class VerifyEmailComponent {
  state: 'idle' | 'loading' | 'emailSent' | 'error' = 'idle';
  emailSent: boolean = false;
  error: any;
  @Input() userEmail: string;

  constructor(
    private userService: UserService,
    private activeModal: NgbActiveModal,
  ) {
  }

  closeModal() {
    this.activeModal.close();
  }

  sendVerifyEmail() {
    this.state = 'loading';
    const req: SendVerifyEmailRequest = new SendVerifyEmailRequest();
    req.email = this.userEmail;
    this.userService.sendVerifyEmail(req).subscribe(
      () => this.handleEmailSentSuccess(),
      (error) => this.handleEmailSentError(error)
    );
  }

  private handleEmailSentSuccess() {
    this.emailSent = true;
    this.state = 'emailSent';
  }

  private handleEmailSentError(err: any) {
    console.error('Error sending verification email:', err);
    this.error = err.message || 'An error occurred while sending the verification email.';
    this.state = 'error';
  }
}

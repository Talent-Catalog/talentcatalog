import {Component, Input} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {SendVerifyEmailRequest} from '../../../model/user';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

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
      (err) => this.handleEmailSentError(err)
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

  getActionText(): string {
    switch (this.state) {
      case 'idle':
        return 'Send Email';
      case 'emailSent':
      case 'error':
        return 'Resend Email';
      case 'loading':
        return 'Loading...';
      default:
        return 'Send Email';
    }
  }
}

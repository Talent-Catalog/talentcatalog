import {Component, Input} from '@angular/core';
import {UserService} from '../../../services/user.service';
import {SendVerifyEmailRequest} from '../../../model/user';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import { faEnvelope, faEnvelopeOpen, faTriangleExclamation } from '@fortawesome/free-solid-svg-icons';

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
  faEnvelope: any = faEnvelope;
  faEnvelopeOpen: any = faEnvelopeOpen;
  faTriangleExclamation: any = faTriangleExclamation;

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

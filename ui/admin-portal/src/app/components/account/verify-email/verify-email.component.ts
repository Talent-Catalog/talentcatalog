import { Component, Input, Output, EventEmitter, OnInit, ViewChild, ElementRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../../services/user.service';
import { SendVerifyEmailRequest, User } from '../../../model/user';
import * as bootstrap from 'bootstrap';
import { AuthenticationService } from "../../../services/authentication.service";

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss'],
})
export class VerifyEmailComponent implements OnInit {
  state: 'idle' | 'loading' | 'emailSent' | 'error' | 'emailVerified' = 'idle';
  emailSent: boolean = false;
  userEmail: string;
  user: User;
  error: boolean = false;
  errorMessage: string = "error message";
  @Input() title: string = 'Email Verification';
  @Output() confirm = new EventEmitter<void>();
  private bootstrapModal: any;

  constructor(
      private http: HttpClient,
      private userService: UserService,
      private route: ActivatedRoute,
      private router: Router,
      private authenticationService: AuthenticationService,
  ) {}

  ngOnInit() {
    this.user = this.authenticationService.getLoggedInUser();
    this.getUserEmail();
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.verifyToken(token);
    }
  }

  verifyToken(token: string) {
    const request = { token: token };
    this.userService.checkEmailVerificationToken(request).subscribe(
        () => this.handleTokenVerificationSuccess(request),
        (error) => this.handleTokenVerificationError(error)
    );
  }

  handleTokenVerificationSuccess(request: { token: string }) {
    this.state = 'emailVerified';
    this.userService.verifyEmail(request).subscribe(
        () => this.handleEmailVerificationSuccess(),
        (error) => this.handleEmailVerificationError(error)
    );
  }

  handleTokenVerificationError(error: any) {
    this.state = 'error';
    const regex = /:\s*(.*)/;
    this.errorMessage = error.match(regex)[1];
    if (this.state === 'error') {
      this.openModal();
    }
  }

  handleEmailVerificationSuccess() {
    this.state = 'emailVerified';
    this.openModal();
    setTimeout(() => {
      this.router.navigate(['/']);
      this.onConfirm();
    }, 3000);
  }

  handleEmailVerificationError(error: any) {
    console.error("error", error.message);
    this.errorMessage = error.message;
    this.state = 'error';
    console.log(this.state);
  }

  checkUserEmailVerificationToken() {
    const token = this.route.snapshot.queryParamMap.get('token');
    this.userService.get(this.user.id).subscribe(
        (user) => this.updateStateBasedOnToken(user, token),
        (error) => this.handleUserRetrievalError(error)
    );
  }

  updateStateBasedOnToken(user: User, token: string | null) {
    if (user.emailVerificationToken && !token) {
      const issuedDate = new Date(user.emailVerificationTokenIssuedDate);
      const currentDate = new Date();
      const hoursDifference = (currentDate.getTime() - issuedDate.getTime()) / (1000 * 60 * 60);
      this.state = hoursDifference > 24 ? 'idle' : 'emailSent';
    }
  }

  handleUserRetrievalError(error: any) {
    this.state = 'error';
    console.error("Error retrieving user info:", error);
  }

  onConfirm() {
    this.confirm.emit();
    this.closeModal();
  }

  openModal() {
    this.checkUserEmailVerificationToken();
    console.log("before opening modal", this.state);
    const modalElement = document.getElementById('verifyEmailModal');
    if (modalElement) {
      this.bootstrapModal = new bootstrap.Modal(modalElement);
      this.bootstrapModal.show();
    }
  }

  getUserEmail() {
    this.userService.get(this.user.id).subscribe(
        (user) => this.userEmail = user.email,
        (error) => this.handleUserEmailRetrievalError(error)
    );
  }

  handleUserEmailRetrievalError(error: any) {
    console.error("Error retrieving user email:", error);
  }


  closeModal() {
    if (this.bootstrapModal) {
      this.bootstrapModal.hide();
      const modalBackdrop = document.querySelector('.modal-backdrop');
      if (modalBackdrop) {
        modalBackdrop.remove();
      }
    }
    this.router.navigate(['/jobs']);
  }

  sendVerifyEmail(token: string) {
    this.state = 'loading';
    const req: SendVerifyEmailRequest = new SendVerifyEmailRequest();
    req.reCaptchaV3Token = token;
    this.userService.get(this.user.id).subscribe(
        (user) => this.sendVerificationEmail(req),
        (error) => this.handleUserEmailRetrievalError(error)
    );
  }

  sendVerificationEmail(req: SendVerifyEmailRequest) {
    req.email = this.userEmail;
    this.userService.sendVerifyEmail(req).subscribe(
        () => this.handleEmailSentSuccess(),
        (error) => this.handleEmailSentError(error)
    );
  }

  handleEmailSentSuccess() {
    this.emailSent = true;
    this.state = 'emailSent';
  }

  handleEmailSentError(error: any) {
    console.error('Error sending verification email:', error);
    this.state = 'error';
  }
}

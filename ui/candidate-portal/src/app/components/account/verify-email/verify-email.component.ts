import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {UserService} from '../../../services/user.service';
import {SendVerifyEmailRequest} from '../../../model/user';
import * as bootstrap from 'bootstrap';

@Component({
    selector: 'app-verify-email',
    templateUrl: './verify-email.component.html',
    styleUrls: ['./verify-email.component.scss'],
})
export class VerifyEmailComponent implements OnInit {
    state: 'idle' | 'loading' | 'emailSent' | 'error' | 'emailVerified' = 'idle';
    emailSent: boolean = false;
    userEmail: string;
    error: boolean = false;
    errorMessage: "error  message";
    @Input() title: string = 'Email Verification'; // The title of the modal
    @Output() confirm = new EventEmitter<void>(); // Emits an event when "Confirm" is clicked
    private bootstrapModal: any; // Store modal instance

    constructor(
        private http: HttpClient,
        private userService: UserService,
        private route: ActivatedRoute,
        private router: Router
    ) {
    }

    ngOnInit() {
        this.getUserEmail();

        // Read the token from the query parameters
        const token = this.route.snapshot.queryParamMap.get('token');

        if (token) {
            const request = {token: token};

            // Step 1: Check if the token is valid
            this.userService.checkEmailVerificationToken(request).subscribe(
                () => {
                    // Proceed to success state
                    this.state = 'emailVerified';

                    // Step 2: If token is valid, verify the email
                    this.userService.verifyEmail(request).subscribe(
                        () => {
                            this.state = 'emailVerified';
                            this.openModal();
                            setTimeout(() => {
                                this.router.navigate(['/']);
                                this.onConfirm() // Change to the desired route
                            }, 3000); // 3-second delay for user feedback

                        },
                        (error) => {
                            console.error("error", error.message);
                            this.errorMessage = error.message;
                            this.state = 'error'; // Handle email verification failure
                            this.openModal();
                        }
                    );
                },
                (error) => {
                    console.error("error2", error);
                    const regex = /:\s*(.*)/;
                    this.errorMessage = error.match(regex)[1];
                    this.state = 'error'; // Handle invalid token
                    this.openModal();
                }
            );
        }
    }

    checkUserEmailVerificationToken() {
        const token = this.route.snapshot.queryParamMap.get('token');
        this.userService.getMyUser().subscribe(
            (user) => {
                if (user.emailVerificationToken && !token) {
                    const issuedDate = new Date(user.emailVerificationTokenIssuedDate);
                    const currentDate = new Date();
                    const hoursDifference = (currentDate.getTime() - issuedDate.getTime()) / (1000 * 60 * 60);

                    if (hoursDifference > 24) {
                        this.state = 'idle';
                    } else {
                        this.state = 'emailSent';
                    }
                }
            },
            (error) => {
                this.state = 'error';
                console.error("Error retrieving user info:", error);
            }
        );
    }

    onConfirm() {
        this.confirm.emit(); // Notify parent component
        this.closeModal();
    }

    openModal() {
        this.checkUserEmailVerificationToken();


        const modalElement = document.getElementById('verifyEmailModal');
        if (modalElement) {
            this.bootstrapModal = new bootstrap.Modal(modalElement);
            this.bootstrapModal.show();
        }


    }

    getUserEmail() {
        this.userService.getMyUser().subscribe(
            (user) => {
                this.userEmail = user.email; // Assign user email
            },
            (error) => {
                console.error("Error retrieving user email:", error);
            }
        );
    }

    closeModal() {
        if (this.bootstrapModal) {
            this.bootstrapModal.hide();
            this.router.navigate(['/']);// ✅ Redirect after closing
        }
    }

    sendVerifyEmail(token: string) {
        this.state = 'loading'; // Set loading state

        const req: SendVerifyEmailRequest = new SendVerifyEmailRequest();
        req.reCaptchaV3Token = token; // Assign token early

        this.userService.getMyUser().subscribe(
            (user) => {

                req.email = this.userEmail; // Now the email is set

                this.userService.sendVerifyEmail(req).subscribe(
                    () => {
                        this.emailSent = true; // Show success message
                        this.state = 'emailSent'; // Store success state
                    },
                    (error) => {
                        console.error('Error sending verification email:', error);
                        this.state = 'error'; // Store error state
                    }
                );
            },
            (error) => {
                console.error('Error fetching user email:', error);
                this.error = true; // Handle error if fetching user email fails
                this.state = 'error';
            }
        );
    }
}

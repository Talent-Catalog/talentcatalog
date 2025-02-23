import {Component, Input} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {VerifyEmailComponent} from '../verify-email/verify-email.component';

@Component({
  selector: 'app-verify-email-toast',
  templateUrl: './verify-email-toast.component.html',
  styleUrls: ['./verify-email-toast.component.scss']
})
export class VerifyEmailToastComponent {
  @Input() showToast: boolean = false;
  @Input() userEmail: string;

  constructor(
    private modalService: NgbModal,
  ) {
  }

  openModal() {
    const verifyEmailModal = this.modalService.open(VerifyEmailComponent, {
      centered: true
    });
    verifyEmailModal.componentInstance.userEmail = this.userEmail;
  }

  hideToast() {
    this.showToast = false;
  }
}

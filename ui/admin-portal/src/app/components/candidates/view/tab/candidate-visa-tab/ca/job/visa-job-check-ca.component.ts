import {Component} from '@angular/core';
import {IeltsStatus} from "../../../../../../../model/candidate";
import {VisaJobCheckBase} from "../../../../../../util/visa/visaJobCheckBase";

@Component({
  selector: 'app-visa-job-check-ca',
  templateUrl: './visa-job-check-ca.component.html',
  styleUrls: ['./visa-job-check-ca.component.scss']
})
export class VisaJobCheckCaComponent extends VisaJobCheckBase {
  partnerIeltsString: string;
  pathwaysInfoLink: string;

  error: string;
  loading: boolean;

  ngOnInit(): void {
    super.ngOnInit();
    // Set the partner IELTS score
    if (this.candidateIntakeData?.partnerIelts) {
      this.partnerIeltsString = IeltsStatus[this.candidateIntakeData?.partnerIelts] +
        (this.candidateIntakeData?.partnerIeltsScore ? ', Score: ' + this.candidateIntakeData.partnerIeltsScore : null);
    } else {
      this.partnerIeltsString = null;
    }
  }
}





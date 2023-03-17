import {Component, OnInit} from '@angular/core';
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {VisaPathway, VisaPathwayService} from "../../../../services/visa-pathway.service";

@Component({
  selector: 'app-visa-pathways',
  templateUrl: './visa-pathways.component.html',
  styleUrls: ['./visa-pathways.component.scss']
})
export class VisaPathwaysComponent extends JobIntakeComponentBase implements OnInit {

  visaPathwayOptions: VisaPathway[];

  constructor(fb: FormBuilder,
              jobService: JobService,
              private visaPathwayService: VisaPathwayService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.getVisaPathwayOptions();
    this.form = this.fb.group({
      visaPathways: [{value: this.jobIntakeData?.visaPathways, disabled: !this.editable}],
    });
  }

  /**
   * Get the visa pathways depending on the country of the job opportunity. Perhaps a better way to do this than an if statement...
   */
  getVisaPathwayOptions() {
    // todo Can we make the country associated with a Job a country object in the DTO so that I can use IDs as opposed to names
    if (this.job.country == "Australia") {
      /**
       * Method stub to get all visa pathways from server depending on the country, using Australia as example.
       */
      this.visaPathwayService.listVisaPathwaysAU().subscribe(
        (results) => {
          this.visaPathwayOptions = results;
        }
      )
    }
  }


}

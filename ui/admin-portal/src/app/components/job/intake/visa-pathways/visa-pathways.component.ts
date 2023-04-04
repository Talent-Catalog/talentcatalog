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

  // todo: temporary options to use for display now. Will eventually have all visas from table.
  public visaOtherOptions: VisaPathway[] = [
      {name: "482 temporary skilled (medium stream)"},
      {name: "186 direct entry permanent stream"},
      {name: "494"}];
  visaPathwayOptions: VisaPathway[];

  constructor(fb: FormBuilder,
              jobService: JobService,
              private visaPathwayService: VisaPathwayService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaPathways: [{value: this.jobIntakeData?.visaPathways, disabled: !this.editable}],
    });
    this.getVisaPathwayOptions();
  }

  get selectedVisaPathways(): VisaPathway[] {
    return this.form.value.visaPathways;
  }

  getVisaPathwayOptions() {
      /**
       * Method stub to get all visa pathways from server depending on the country, using Australia as example.
       */
      let countryId = 6191
      this.visaPathwayService.getVisaPathwaysCountry(countryId).subscribe(
        (results) => {
          //this.visaPathwayOptions = results;
        }
      )
    // todo REMOVE hardcoded temporary visa pathway options
    this.visaPathwayOptions = [
      {name: "482: Temporary Skill Shortage",
        description: "This temporary visa lets an employer sponsor a suitably skilled worker to fill a position they can’t find a suitably skilled Australian to fill.",
        age: "< 50 yrs old"},
      {name: "186: Employer Nomination Scheme",
        description: "This visa lets skilled workers, who are nominated by their employer, live and work in Australia permanently."},
      {name: "494: Skilled Employer Sponsored Regional (Provisional)",
        description: "This visa enables regional employers to address identified labour shortages within their region by sponsoring skilled workers where employers can't source an appropriately skilled Australian worker."}];
  }


}

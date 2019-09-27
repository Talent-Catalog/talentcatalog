import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {years} from "../../../model/years";
import {CandidateEducation} from "../../../model/candidate-education";
import {CandidateService} from "../../../services/candidate.service";
import {EducationService} from "../../../services/education.service";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";


@Component({
  selector: 'app-registration-masters',
  templateUrl: './registration-masters.component.html',
  styleUrls: ['./registration-masters.component.scss']
})
export class RegistrationMastersComponent implements OnInit {

  form: FormGroup;
  error: any;
  loading: boolean;
  saving: boolean;
  countries: Country[];
  years: number[];
  educations: CandidateEducation[];
  masters: CandidateEducation[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private educationService: EducationService,
              private countryService: CountryService) { }

  ngOnInit() {
    this.countries = [];
    this.years = years;
    this.saving = false;
    this.loading = true;

    this.form = this.fb.group({
      educationType: ['Masters'],
      courseName: ['', Validators.required],
      countryId: ['', Validators.required],
      institution: ['', Validators.required],
      lengthOfCourseYears: ['', Validators.required],
      dateCompleted: ['', Validators.required]
     });

     /* Load & update the candidate data */
     this.candidateService.getCandidateEducations().subscribe(
       (candidate) => {
         this.educations = candidate.educations || [];

         /* filter for the correct education type for the component */
         this.masters = this.educations.filter(e => e.educationType == "Masters");
         if(this.masters.length !== 0){
          this.form.patchValue({
             educationType: this.masters[0].educationType,
             courseName: this.masters[0].courseName,
             countryId: this.masters[0].country.id,
             institution: this.masters[0].institution,
             lengthOfCourseYears: this.masters[0].lengthOfCourseYears,
             dateCompleted: this.masters[0].dateCompleted,
           });
         }

        /* Load the countries */
         this.countryService.listCountries().subscribe(
         (response) => {
           this.countries = response;
           this.loading = false;
           },
         (error) => {
           this.error = error;
           this.loading = false;
           }
         );
       },

       (error) => {
         this.error = error;
         this.loading = false;
       }
     );
   };

   save() {
     this.saving = true;
     /* CREATE if no masters education type exists in education table*/

     if(this.masters.length == 0){
       this.educationService.createEducation(this.form.value).subscribe(
         (response) => {
            console.log(response);
            this.educations.push(response);
            this.saving = false;
            this.router.navigate(['register', 'education', 'university']);
         },
         (error) => {
            this.error = error;
            this.saving = false;
         },
       );

     /* UPDATE if masters education type exists */

     } else {
       this.educationService.updateEducation(this.form.value).subscribe(
         (response) => {
            this.router.navigate(['register', 'education', 'university']);
         },
         (error) => {
            this.error = error;
            this.saving = false;
         }
       );
     }
   }
}

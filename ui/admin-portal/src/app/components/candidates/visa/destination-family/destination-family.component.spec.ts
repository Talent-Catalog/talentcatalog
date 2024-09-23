import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DestinationFamilyComponent} from './destination-family.component';
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {By} from "@angular/platform-browser";

describe('DestinationFamilyComponent', () => {
  let component: DestinationFamilyComponent;
  let fixture: ComponentFixture<DestinationFamilyComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      declarations: [DestinationFamilyComponent,AutosaveStatusComponent],
      providers: [
        FormBuilder,
        {provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy}
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationFamilyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('location input should be displayed when visaDestinationFamily is a family relation', () => {
    component.form.controls['visaDestinationFamily'].setValue('Child');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaDestinationFamilyLocation'));
    expect(textarea).toBeTruthy();
  });

  it('location input should not be displayed when visaDestinationFamily is "NoRelation" or null', () => {
    component.form.controls['visaDestinationFamily'].setValue('NoRelation');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaDestinationFamilyLocation'));
    expect(textarea).toBeFalsy();

    component.form.controls['visaDestinationFamily'].setValue(null);
    fixture.detectChanges();
    expect(textarea).toBeFalsy();
  });
});

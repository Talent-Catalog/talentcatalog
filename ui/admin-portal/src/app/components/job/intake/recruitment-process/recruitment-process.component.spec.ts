import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {RecruitmentProcessComponent} from './recruitment-process.component';
import {NgxWigModule} from 'ngx-wig';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

fdescribe('RecruitmentProcessComponent', () => {
  let component: RecruitmentProcessComponent;
  let fixture: ComponentFixture<RecruitmentProcessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RecruitmentProcessComponent, AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule, NgxWigModule, HttpClientTestingModule ],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RecruitmentProcessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form control with correct initial value and disabled state', () => {
    // Access the form control
    const recruitmentProcessControl = component.form.get('recruitmentProcess');

    // Check if the form control is initialized
    expect(recruitmentProcessControl).toBeTruthy();

    // Check if the initial value is set correctly
    expect(recruitmentProcessControl.value).toEqual(component.jobIntakeData?.recruitmentProcess);

    // Check if the disabled state is set correctly
    expect(recruitmentProcessControl.disabled).toBe(!component.editable);
  });

  it('should make form control valid if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    expect(component.form.get('recruitmentProcess').valid).toBe(true);
  });

  it('should update form control value on input change', () => {
    // Simulate user input
    const newRecruitmentProcess = 'Outline of the new recruitment process';
    component.editable = true; // Enable editing
    fixture.detectChanges();
    const recruitmentProcess = fixture.nativeElement.querySelector('.nw-editor__res');
    recruitmentProcess.innerText = newRecruitmentProcess;
    recruitmentProcess.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    // Check if the form control value is updated correctly
    expect(component.form.get('recruitmentProcess').value).toEqual(newRecruitmentProcess);
  });

});

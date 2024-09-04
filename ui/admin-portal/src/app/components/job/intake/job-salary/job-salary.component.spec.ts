import {JobSalaryComponent} from "./job-salary.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {NgxWigModule} from "ngx-wig";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('JobSalaryComponent', () => {
  let component: JobSalaryComponent;
  let fixture: ComponentFixture<JobSalaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobSalaryComponent,AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule,NgxWigModule,HttpClientTestingModule ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSalaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form control value based on jobIntakeData', () => {
    const initialSalaryRange = '$50,000 - $70,000';
    component.editable = true;
    component.jobIntakeData = { salaryRange: initialSalaryRange };

    component.ngOnInit();

    expect(component.form.get('salaryRange').value).toEqual(initialSalaryRange);
  });

  it('should make form control valid if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    expect(component.form.get('salaryRange').valid).toBe(true);
  });

  it('should update form control value on input change', fakeAsync(() => {
    const initialSalary = '$10,000 - $20,000';
    component.editable = true;
    component.jobIntakeData = { salaryRange: initialSalary };
    component.ngOnInit();

    expect(component.form.get('salaryRange').value).toEqual(initialSalary);


    const newSalaryRange = '$60,000 - $80,000';
    component.editable = true;
    fixture.detectChanges();

    const salaryRangeInput = fixture.nativeElement.querySelector('#salaryRange');
    salaryRangeInput.value = newSalaryRange;
    salaryRangeInput.dispatchEvent(new Event('input'));
    tick();

    expect(component.form.get('salaryRange').value).toEqual(newSalaryRange);
  }));
});

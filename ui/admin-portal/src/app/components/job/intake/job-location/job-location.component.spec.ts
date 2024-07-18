import {JobLocationComponent} from "./job-location.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

fdescribe('JobLocationComponent', () => {
  let component: JobLocationComponent;
  let fixture: ComponentFixture<JobLocationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobLocationComponent, AutosaveStatusComponent ],
      imports: [ReactiveFormsModule ,HttpClientTestingModule],

    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobLocationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display error message when error is present', () => {
    const error = 'An error occurred';
    component.error = error;
    fixture.detectChanges();
    const errorElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain(error);
  });

  it('should not display error message when error is not present', () => {
    const error = null;
    component.error = error;
    fixture.detectChanges();
    const errorElement = fixture.nativeElement.querySelector('.error');
    expect(errorElement).toBeNull();
  });

  it('should render autosave status component when editable', () => {
    component.editable = true;
    fixture.detectChanges();
    const autosaveStatusComponent = fixture.nativeElement.querySelector('app-autosave-status');
    expect(autosaveStatusComponent).toBeTruthy();
  });

  it('should not render autosave status component when not editable', () => {
    component.editable = false;
    fixture.detectChanges();
    const autosaveStatusComponent = fixture.nativeElement.querySelector('app-autosave-status');
    expect(autosaveStatusComponent).toBeNull();
  });

  it('should disable form control when not editable', () => {
    component.editable = false;
    component.jobIntakeData = { location: 'San Francisco' };
    fixture.detectChanges();
    component.ngOnInit();
    expect(component.form.get('location').disabled).toBeTrue();
  });
  it('should update form control value on input change', () => {
    const initialLocation = 'San Francisco';
    component.editable = true;
    component.jobIntakeData = { location: initialLocation };
    component.ngOnInit();

    expect(component.form.get('location').value).toEqual(initialLocation);

    const newLocation = 'New York';
    component.editable = true;
    fixture.detectChanges();

    const locationInput = fixture.nativeElement.querySelector('#location');
    locationInput.value = newLocation;
    locationInput.dispatchEvent(new Event('input'));

    expect(component.form.get('location').value).toEqual(newLocation);
  });

 });

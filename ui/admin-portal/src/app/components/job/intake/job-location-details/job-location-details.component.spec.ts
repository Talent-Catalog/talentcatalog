import {JobLocationDetailsComponent} from "./job-location-details.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgxWigModule} from "ngx-wig";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('JobLocationDetailsComponent', () => {
  let component: JobLocationDetailsComponent;
  let fixture: ComponentFixture<JobLocationDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobLocationDetailsComponent,AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule,NgxWigModule, HttpClientTestingModule ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobLocationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form control value based on jobIntakeData', () => {
    const initialLocationDetails = 'City with a population of 1 million';
    component.editable = true;
    component.jobIntakeData = { locationDetails: initialLocationDetails };

    component.ngOnInit();

    expect(component.form.get('locationDetails').value).toEqual(initialLocationDetails);
  });

  it('should make form control valid if editable', () => {
    component.editable = true;
    fixture.detectChanges();

    expect(component.form.get('locationDetails').valid).toBe(true);
  });

  it('should update form control value on input change', fakeAsync(() => {
    const initialLocation = 'San Francisco';
    component.editable = true;
    component.jobIntakeData = { locationDetails: initialLocation };
    component.ngOnInit();

    expect(component.form.get('locationDetails').value).toEqual(initialLocation);

    const newLocationDetails = 'Rural area with low cost of living';
    component.editable = true;
    fixture.detectChanges();

    const locationDetailsDiv = fixture.nativeElement.querySelector('.nw-editor__res');
    locationDetailsDiv.innerText = newLocationDetails;
    locationDetailsDiv.dispatchEvent(new Event('input'));
    tick();

    expect(component.form.get('locationDetails').value).toEqual(newLocationDetails);
  }));
});

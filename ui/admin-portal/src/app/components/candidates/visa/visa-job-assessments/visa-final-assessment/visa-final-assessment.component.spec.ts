import {ComponentFixture, TestBed} from '@angular/core/testing';
import {VisaFinalAssessmentComponent} from './visa-final-assessment.component';
import {By} from '@angular/platform-browser';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {LocalStorageModule} from "angular-2-local-storage";
import {MockCandidate} from "../../../../../MockData/MockCandidate";

describe('VisaFinalAssessmentComponent', () => {
  let component: VisaFinalAssessmentComponent;
  let fixture: ComponentFixture<VisaFinalAssessmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule,LocalStorageModule.forRoot({})],
      declarations: [VisaFinalAssessmentComponent,AutosaveStatusComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaFinalAssessmentComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display loading spinner when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const loadingElement = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(loadingElement).toBeTruthy();
  });

  it('should not display loading spinner or error message when loading is false and no error', () => {
    component.loading = false;
    fixture.detectChanges();
    const loadingElement = fixture.debugElement.query(By.css('.fa-spinner'));
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('.error');
    expect(loadingElement).toBeFalsy();
    expect(errorElement).toBeFalsy();
  });

});

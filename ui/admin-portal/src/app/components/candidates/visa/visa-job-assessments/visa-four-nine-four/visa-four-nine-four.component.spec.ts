import {ComponentFixture, TestBed} from '@angular/core/testing';
import {VisaFourNineFourComponent} from './visa-four-nine-four.component';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('VisaFourNineFourComponent', () => {
  let component: VisaFourNineFourComponent;
  let fixture: ComponentFixture<VisaFourNineFourComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisaFourNineFourComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [FormBuilder] // Provide the FormBuilder
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaFourNineFourComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display error message if error is set', () => {
    component.error = 'Some error occurred';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error occurred');
  });

  it('should display notes section when eligible for 494', () => {
    component.form.get('visaJobEligible494')?.setValue('Yes');
    component.ngOnInit();
    const notesElement: HTMLElement = fixture.nativeElement.querySelector('.mb-3');
    expect(notesElement).toBeTruthy();
  });

});

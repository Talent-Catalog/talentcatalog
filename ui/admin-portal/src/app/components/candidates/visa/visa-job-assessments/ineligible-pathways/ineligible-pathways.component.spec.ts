import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {IneligiblePathwaysComponent} from './ineligible-pathways.component';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('IneligiblePathwaysComponent', () => {
  let component: IneligiblePathwaysComponent;
  let fixture: ComponentFixture<IneligiblePathwaysComponent>;
  let fb: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IneligiblePathwaysComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule],
      providers: [FormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IneligiblePathwaysComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with visaJobIneligiblePathways control', () => {
    expect(component.form.contains('visaJobIneligiblePathways')).toBeTrue();
  });

  it('should update form value when input changes', () => {
    const testValue = 'Test ineligible pathways';
    const textarea = fixture.nativeElement.querySelector('textarea');
    textarea.value = testValue;
    textarea.dispatchEvent(new Event('input'));
    expect(component.form.get('visaJobIneligiblePathways')?.value).toEqual(testValue);
  });
});

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {IeltsLevelComponent} from './ielts-level.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

fdescribe('IeltsLevelComponent', () => {
  let component: IeltsLevelComponent;
  let fixture: ComponentFixture<IeltsLevelComponent>;
  let fb: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IeltsLevelComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule],
      providers: [FormBuilder]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IeltsLevelComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have a form with ieltsLevel control', () => {
    expect(component.form.contains('ieltsLevel')).toBeTrue();
  });

  it('should display error message for required field', () => {
    const compiled = fixture.nativeElement;
    const input = compiled.querySelector('input');
    input.value = '';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    const errorMessage = compiled.querySelector('small').textContent;
    expect(errorMessage).toContain('Must be minimum 4.5 IELTS, average across all bands.');
  });

  it('should display error message for minimum value', () => {
    const compiled = fixture.nativeElement;
    const input = compiled.querySelector('input');
    input.value = '4.0';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    const errorMessage = compiled.querySelector('small').textContent;
    expect(errorMessage).toContain('Must be minimum 4.5 IELTS, average across all bands.');
  });
});

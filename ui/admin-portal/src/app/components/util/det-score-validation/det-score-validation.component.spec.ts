import {DetScoreValidationComponent} from './det-score-validation.component';
import {ComponentFixture, TestBed, fakeAsync} from '@angular/core/testing';
import {UntypedFormControl, FormsModule, ReactiveFormsModule} from "@angular/forms";


describe('DetScoreValidationComponent', () => {
  let component: DetScoreValidationComponent;
  let fixture: ComponentFixture<DetScoreValidationComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DetScoreValidationComponent],
      imports: [FormsModule, ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(DetScoreValidationComponent);
    component = fixture.componentInstance;
    component.control = new UntypedFormControl();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize regex and value correctly', () => {
    component.ngOnInit();
    expect(component.regex).toEqual(new RegExp('^(1[0-9]|[2-9][0-9]|1[0-5][0-9]|160)$'));
    expect(component.value).toBeNull();
  });

  it('should set value from control if available', () => {
    component.control.setValue('50');
    component.ngOnInit();
    expect(component.value).toBe('50');
  });

  it('should update control value on valid input', () => {
    component.value = '140';
    component.update();
    expect(component.control.value).toBe('140');
    expect(component.error).toBeNull();
  });

  it('should show error and clear input on invalid input', fakeAsync(() => {
    component.value = '170';
    component.update();
    expect(component.control.value).toBe(0);
    expect(component.error).toBe('DET grades are always a whole number between 10 and 160.');
  }));

  it('should set control value to 0 if input is null', () => {
    component.value = null;
    component.update();
    expect(component.control.value).toBe(0);
  });
});

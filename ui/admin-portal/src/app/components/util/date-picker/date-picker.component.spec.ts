import {DatePickerComponent} from "./date-picker.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AbstractControl, UntypedFormControl, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {DebugElement} from "@angular/core";
import {By} from "@angular/platform-browser";

describe('DatePickerComponent', () => {
  let component: DatePickerComponent;
  let fixture: ComponentFixture<DatePickerComponent>;
  let control: AbstractControl;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DatePickerComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbDatepickerModule]
    }).compileComponents();

    fixture = TestBed.createComponent(DatePickerComponent);
    component = fixture.componentInstance;
    control = new UntypedFormControl('');
    component.control = control;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

    it('should initialize with correct date values and min/max dates', () => {
      component.ngOnInit();
      expect(component.date).toBe('');
      expect(component.today).toBeDefined();

      if (component.allowFuture) {
        expect(component.maxDate).toBeNull();
      } else {
        expect(component.maxDate).toEqual({
          year: component.today.getFullYear(),
          month: component.today.getMonth() + 1,
          day: component.today.getDate()
        });
      }

      if (component.allowPast) {
        expect(component.minDate).toEqual({
          year: component.today.getFullYear() - 100,
          month: 1,
          day: 1
        });
      } else {
        expect(component.minDate).toEqual({
          year: component.today.getFullYear(),
          month: component.today.getMonth() + 1,
          day: component.today.getDate()
        });
      }
    });

    it('should update control value if date is valid', () => {
      component.date = '2024-07-01';
      component.update();
      expect(component.error).toBeNull();
      expect(control.value).toBe('2024-07-01');
    });

    it('should set error if date is invalid', () => {
      component.date = '2024-13-01';
      component.update();
      expect(component.error).toBe('Incorrect date format, please type date in yyyy-mm-dd');
      expect(control.value).toBe('');
    });

    it('should set error if date format is invalid', () => {
      component.date = '01-2024-07';
      component.update();
      expect(component.error).toBe('Incorrect date format, please type date in yyyy-mm-dd');
      expect(control.value).toBe('');
    });

    it('should clear the date and control value', () => {
      component.date = '2024-07-01';
      control.patchValue(component.date);
      component.clear();
      expect(component.date).toBeNull();
      expect(control.value).toBeNull();
    });

    it('should display error message if error is present', () => {
      component.error = 'Test error message';
      fixture.detectChanges();
      const errorMessage: DebugElement = fixture.debugElement.query(By.css('.alert-danger'));
      expect(errorMessage.nativeElement.textContent).toContain('Test error message');
    });

    it('should call update method when date changes', () => {
      spyOn(component, 'update');
      const input: DebugElement = fixture.debugElement.query(By.css('input[name="dp"]'));
      input.nativeElement.value = '2024-07-01';
      input.triggerEventHandler('ngModelChange', '2024-07-01');
      fixture.detectChanges();
      expect(component.update).toHaveBeenCalled();
    });

    it('should call clear method when Clear button is clicked', () => {
      spyOn(component, 'clear');
      const clearButton: DebugElement = fixture.debugElement.query(By.css('.btn-danger'));
      clearButton.nativeElement.click();
      expect(component.clear).toHaveBeenCalled();
    });

});

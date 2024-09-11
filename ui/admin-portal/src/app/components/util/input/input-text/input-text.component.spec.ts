import {InputTextComponent} from "./input-text.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('InputTextComponent', () => {
  let component: InputTextComponent;
  let fixture: ComponentFixture<InputTextComponent>;
  let modalMock: Partial<NgbActiveModal>;

  beforeEach(async () => {
    modalMock = {
      close: jasmine.createSpy('close'),
      dismiss: jasmine.createSpy('dismiss')
    };

    await TestBed.configureTestingModule({
      imports: [FormsModule, ReactiveFormsModule],
      declarations: [InputTextComponent],
      providers: [
        { provide: NgbActiveModal, useValue: modalMock }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InputTextComponent);
    component = fixture.componentInstance;
    component.initialText = 'Initial text';
    component.message = 'Please enter text';
    component.nRows = 3;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with initial text', () => {
    expect(component.form.get('text').value).toBe('Initial text');
  });

  it('should dismiss the modal when dismiss() is called', () => {
    component.dismiss();
    expect(modalMock.dismiss).toHaveBeenCalledWith(false);
  });

  it('should close the modal with form value when close() is called', fakeAsync(() => {
    component.form.patchValue({ text: 'Updated text' });
    fixture.detectChanges();

    component.close();
    tick(); // Simulate async operation

    expect(modalMock.close).toHaveBeenCalledWith('Updated text');
  }));

  it('should render the message if message is provided', () => {
    const labelElement: HTMLElement = fixture.nativeElement.querySelector('.form-label');
    expect(labelElement).toBeTruthy();
    expect(labelElement.textContent).toContain('Please enter text');
  });

  it('should not render the message if message is not provided', () => {
    component.message = null;
    fixture.detectChanges();

    const labelElement: HTMLElement = fixture.nativeElement.querySelector('.form-label');
    expect(labelElement).toBeNull();
  });

  it('should render the Cancel button if showCancel is true', () => {
    component.showCancel = true;
    fixture.detectChanges();

    const cancelButton: HTMLElement = fixture.nativeElement.querySelector('.modal-footer .btn:nth-child(2)');
    expect(cancelButton).toBeTruthy();
    expect(cancelButton.textContent).toContain('Cancel');
  });

  it('should not render the Cancel button if showCancel is false', () => {
    component.showCancel = false;
    fixture.detectChanges();

    const cancelButton: HTMLElement = fixture.nativeElement.querySelector('.modal-footer .btn:nth-child(2)');
    expect(cancelButton).toBeNull();
  });
});

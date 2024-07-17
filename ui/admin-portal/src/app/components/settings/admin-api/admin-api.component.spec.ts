import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {AdminApiComponent} from './admin-api.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('AdminApiComponent', () => {
  let component: AdminApiComponent;
  let fixture: ComponentFixture<AdminApiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminApiComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminApiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with an empty "apicall" control', () => {
    const apicallControl = component.form.get('apicall');
    expect(apicallControl).toBeTruthy();
    expect(apicallControl.value).toBe(null);
  });

  it('should update the form control value when the input value changes', () => {
    const input = fixture.debugElement.query(By.css('input')).nativeElement;
    input.value = 'test api call';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(component.form.get('apicall').value).toBe('test api call');
  });

  it('should call send method when the button is clicked', () => {
    spyOn(component, 'send');

    const button = fixture.debugElement.query(By.css('button')).nativeElement;
    button.click();

    expect(component.send).toHaveBeenCalled();
  });

  it('should display the ack message when ack is set', () => {
    component.ack = 'Success!';
    fixture.detectChanges();

    const ackMessage = fixture.debugElement.queryAll(By.css('div'))
    .map(de => de.nativeElement)
    .find(div => div.textContent.includes('Success!'));
    expect(ackMessage.textContent).toContain('Success!');
  });

  it('should display the error message when error is set', () => {
    component.error = 'Error occurred!';
    fixture.detectChanges();

    const errorMessage = fixture.debugElement.queryAll(By.css('div'))
    .map(de => de.nativeElement)
    .find(div => div.textContent.includes('Error occurred!'));
    expect(errorMessage.textContent).toContain('Error occurred!');
  });
});

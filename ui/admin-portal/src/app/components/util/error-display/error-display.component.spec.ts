import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorDisplayComponent } from './error-display.component';
import {By} from "@angular/platform-browser";

fdescribe('ErrorDisplayComponent', () => {
  let component: ErrorDisplayComponent;
  let fixture: ComponentFixture<ErrorDisplayComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ErrorDisplayComponent]
    });
    fixture = TestBed.createComponent(ErrorDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should display the error message when provided', () => {
    const errorMessage = 'An error occurred!';
    component.error = errorMessage;
    fixture.detectChanges(); // Trigger change detection

    const errorDiv = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorDiv).toBeTruthy(); // Check if the error message div exists
    expect(errorDiv.nativeElement.textContent).toContain(errorMessage); // Verify error message content
  });

  it('should not display the error message when error input is not provided', () => {
    component.error = null;
    fixture.detectChanges(); // Trigger change detection

    const errorDiv = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorDiv).toBeNull(); // The error div should not be present
  });

  it('should handle undefined error gracefully', () => {
    component.error = undefined;
    fixture.detectChanges(); // Trigger change detection

    const errorDiv = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorDiv).toBeNull(); // The error div should not be present
  });

});

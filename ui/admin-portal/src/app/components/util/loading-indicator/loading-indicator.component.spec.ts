import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadingIndicatorComponent } from './loading-indicator.component';
import {TranslateModule} from "@ngx-translate/core";
import {By} from "@angular/platform-browser";

fdescribe('LoadingIndicatorComponent', () => {
  let component: LoadingIndicatorComponent;
  let fixture: ComponentFixture<LoadingIndicatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LoadingIndicatorComponent ],
      imports: [ TranslateModule.forRoot() ] // For translation pipe
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoadingIndicatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the loading indicator when loading is true', () => {
    component.loading = true;
    fixture.detectChanges(); // Trigger change detection

    const loadingDiv = fixture.debugElement.query(By.css('.text-start'));
    expect(loadingDiv).toBeTruthy(); // Ensure the loading indicator div is present
    expect(loadingDiv.nativeElement.textContent).toContain('LOADING'); // Check for the loading text (translated)
    expect(loadingDiv.nativeElement.querySelector('i')).toHaveClass('fa-spinner'); // Check for spinner icon
    expect(loadingDiv.nativeElement.querySelector('i')).toHaveClass('fa-spin'); // Ensure spinner is spinning
  });

  it('should not display the loading indicator when loading is false', () => {
    component.loading = false;
    fixture.detectChanges(); // Trigger change detection

    const loadingDiv = fixture.debugElement.query(By.css('.text-start'));
    expect(loadingDiv).toBeNull(); // The loading div should not be present
  });

  it('should handle undefined loading gracefully', () => {
    component.loading = undefined;
    fixture.detectChanges(); // Trigger change detection

    const loadingDiv = fixture.debugElement.query(By.css('.text-start'));
    expect(loadingDiv).toBeNull(); // The loading div should not be present
  });
});

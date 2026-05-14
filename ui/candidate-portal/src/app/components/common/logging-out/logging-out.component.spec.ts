import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoggingOutComponent } from './logging-out.component';

describe('LoggingOutComponent', () => {
  let component: LoggingOutComponent;
  let fixture: ComponentFixture<LoggingOutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoggingOutComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoggingOutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

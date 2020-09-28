import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaIssuesComponent} from './visa-issues.component';

describe('VisaIssuesComponent', () => {
  let component: VisaIssuesComponent;
  let fixture: ComponentFixture<VisaIssuesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaIssuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaIssuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

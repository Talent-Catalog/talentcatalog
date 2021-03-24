import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {HealthIssuesComponent} from './health-issues.component';

describe('HealthIssuesComponent', () => {
  let component: HealthIssuesComponent;
  let fixture: ComponentFixture<HealthIssuesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HealthIssuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HealthIssuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

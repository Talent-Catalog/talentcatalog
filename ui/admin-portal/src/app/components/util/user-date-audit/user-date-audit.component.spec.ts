import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {UserDateAuditComponent} from './user-date-audit.component';

describe('UserDateAuditComponent', () => {
  let component: UserDateAuditComponent;
  let fixture: ComponentFixture<UserDateAuditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserDateAuditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDateAuditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

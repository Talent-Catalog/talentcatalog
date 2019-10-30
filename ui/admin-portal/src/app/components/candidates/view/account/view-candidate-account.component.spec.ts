import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewCandidateAccountComponent} from './view-candidate-account.component';

describe('ViewCandidateAccountComponent', () => {
  let component: ViewCandidateAccountComponent;
  let fixture: ComponentFixture<ViewCandidateAccountComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewCandidateAccountComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateAccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

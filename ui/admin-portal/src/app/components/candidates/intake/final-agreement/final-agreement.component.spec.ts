import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {FinalAgreementComponent} from './final-agreement.component';

describe('FinalAgreementComponent', () => {
  let component: FinalAgreementComponent;
  let fixture: ComponentFixture<FinalAgreementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FinalAgreementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FinalAgreementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

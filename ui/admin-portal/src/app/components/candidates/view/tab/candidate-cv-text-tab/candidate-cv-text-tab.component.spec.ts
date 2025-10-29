import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateCvTextTabComponent} from './candidate-cv-text-tab.component';

describe('CandidateCvTextTabComponent', () => {
  let component: CandidateCvTextTabComponent;
  let fixture: ComponentFixture<CandidateCvTextTabComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CandidateCvTextTabComponent]
    });
    fixture = TestBed.createComponent(CandidateCvTextTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidateTaskTabComponent} from './candidate-task-tab.component';

describe('CandidateTaskTabComponent', () => {
  let component: CandidateTaskTabComponent;
  let fixture: ComponentFixture<CandidateTaskTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidateTaskTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateTaskTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

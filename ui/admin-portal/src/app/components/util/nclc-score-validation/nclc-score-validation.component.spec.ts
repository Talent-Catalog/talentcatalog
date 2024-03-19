import {ComponentFixture, TestBed} from '@angular/core/testing';

import {NclcScoreValidationComponent} from './nclc-score-validation.component';

describe('NclcScoreValidationComponent', () => {
  let component: NclcScoreValidationComponent;
  let fixture: ComponentFixture<NclcScoreValidationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NclcScoreValidationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NclcScoreValidationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

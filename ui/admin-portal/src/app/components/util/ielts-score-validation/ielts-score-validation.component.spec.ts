import {ComponentFixture, TestBed} from '@angular/core/testing';

import {IeltsScoreValidationComponent} from './ielts-score-validation.component';

describe('FormControlConditionalComponent', () => {
  let component: IeltsScoreValidationComponent;
  let fixture: ComponentFixture<IeltsScoreValidationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IeltsScoreValidationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IeltsScoreValidationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

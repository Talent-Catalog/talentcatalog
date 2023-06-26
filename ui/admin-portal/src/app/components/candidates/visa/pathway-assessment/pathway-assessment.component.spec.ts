import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PathwayAssessmentComponent} from './pathway-assessment.component';

describe('CountryPathwayAssessmentComponent', () => {
  let component: PathwayAssessmentComponent;
  let fixture: ComponentFixture<PathwayAssessmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PathwayAssessmentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PathwayAssessmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

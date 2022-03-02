import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EditCandidateMediaWillingnessComponent} from './edit-candidate-media-willingness.component';

describe('EditCandidateMediaWillingnessComponent', () => {
  let component: EditCandidateMediaWillingnessComponent;
  let fixture: ComponentFixture<EditCandidateMediaWillingnessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditCandidateMediaWillingnessComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateMediaWillingnessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

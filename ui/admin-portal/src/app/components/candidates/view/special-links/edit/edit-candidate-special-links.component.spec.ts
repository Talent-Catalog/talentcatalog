import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditCandidateSpecialLinksComponent } from './edit-candidate-special-links.component';

describe('EditCandidateSpecialLinksComponent', () => {
  let component: EditCandidateSpecialLinksComponent;
  let fixture: ComponentFixture<EditCandidateSpecialLinksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditCandidateSpecialLinksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateSpecialLinksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

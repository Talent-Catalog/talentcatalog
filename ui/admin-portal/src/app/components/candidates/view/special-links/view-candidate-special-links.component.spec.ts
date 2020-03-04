import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewCandidateSpecialLinksComponent } from './view-candidate-special-links.component';

describe('ViewCandidateSpecialLinksComponent', () => {
  let component: ViewCandidateSpecialLinksComponent;
  let fixture: ComponentFixture<ViewCandidateSpecialLinksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewCandidateSpecialLinksComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateSpecialLinksComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

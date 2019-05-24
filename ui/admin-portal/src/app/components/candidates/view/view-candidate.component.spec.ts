import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewCandidateComponent } from './view-candidate.component';

describe('ViewCandidateComponent', () => {
  let component: ViewCandidateComponent;
  let fixture: ComponentFixture<ViewCandidateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViewCandidateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCandidateComponent } from './create-candidate.component';

describe('CreateCandidateComponent', () => {
  let component: CreateCandidateComponent;
  let fixture: ComponentFixture<CreateCandidateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateCandidateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

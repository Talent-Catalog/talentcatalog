import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateCandidateLanguageComponent} from './create-candidate-language.component';

describe('CreateCandidateLanguageComponent', () => {
  let component: CreateCandidateLanguageComponent;
  let fixture: ComponentFixture<CreateCandidateLanguageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateCandidateLanguageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateLanguageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

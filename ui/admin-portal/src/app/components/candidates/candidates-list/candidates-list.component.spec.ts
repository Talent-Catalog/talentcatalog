import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CandidatesListComponent} from './candidates-list.component';

describe('CandidatesListComponent', () => {
  let component: CandidatesListComponent;
  let fixture: ComponentFixture<CandidatesListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CandidatesListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidatesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

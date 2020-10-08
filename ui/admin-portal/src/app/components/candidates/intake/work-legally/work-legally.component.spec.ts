import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkLegallyComponent} from './work-legally.component';

describe('WorkLegallyComponent', () => {
  let component: WorkLegallyComponent;
  let fixture: ComponentFixture<WorkLegallyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkLegallyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkLegallyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkPermitComponent} from './work-permit.component';

describe('WorkPermitComponent', () => {
  let component: WorkPermitComponent;
  let fixture: ComponentFixture<WorkPermitComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkPermitComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkPermitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

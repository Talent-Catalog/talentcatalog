import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AvailImmediateComponent} from './avail-immediate.component';

describe('AvailImmediateComponent', () => {
  let component: AvailImmediateComponent;
  let fixture: ComponentFixture<AvailImmediateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AvailImmediateComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AvailImmediateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

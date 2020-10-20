import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DestinationLimitComponent} from './destination-limit.component';

describe('DestinationLimitComponent', () => {
  let component: DestinationLimitComponent;
  let fixture: ComponentFixture<DestinationLimitComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DestinationLimitComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationLimitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

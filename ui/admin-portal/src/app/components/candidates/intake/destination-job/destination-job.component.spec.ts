import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DestinationJobComponent} from './destination-job.component';

describe('DestinationJobComponent', () => {
  let component: DestinationJobComponent;
  let fixture: ComponentFixture<DestinationJobComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DestinationJobComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationJobComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

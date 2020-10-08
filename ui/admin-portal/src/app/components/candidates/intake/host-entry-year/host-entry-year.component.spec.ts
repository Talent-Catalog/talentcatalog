import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {HostEntryYearComponent} from './host-entry-year.component';

describe('HostEntryYearComponent', () => {
  let component: HostEntryYearComponent;
  let fixture: ComponentFixture<HostEntryYearComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HostEntryYearComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HostEntryYearComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

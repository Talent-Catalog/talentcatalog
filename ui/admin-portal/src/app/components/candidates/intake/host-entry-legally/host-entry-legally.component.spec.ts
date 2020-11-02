import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {HostEntryLegallyComponent} from './host-entry-legally.component';

describe('HostEntryLegallyComponent', () => {
  let component: HostEntryLegallyComponent;
  let fixture: ComponentFixture<HostEntryLegallyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HostEntryLegallyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HostEntryLegallyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

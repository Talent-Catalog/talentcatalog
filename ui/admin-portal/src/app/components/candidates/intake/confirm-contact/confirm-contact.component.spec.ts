import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ConfirmContactComponent} from './confirm-contact.component';

describe('ConfirmContactComponent', () => {
  let component: ConfirmContactComponent;
  let fixture: ComponentFixture<ConfirmContactComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfirmContactComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmContactComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

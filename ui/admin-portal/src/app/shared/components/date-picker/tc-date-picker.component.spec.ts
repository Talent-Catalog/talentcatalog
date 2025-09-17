import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDatePickerComponent} from './tc-date-picker.component';

describe('TcDatePickerComponent', () => {
  let component: TcDatePickerComponent;
  let fixture: ComponentFixture<TcDatePickerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDatePickerComponent]
    });
    fixture = TestBed.createComponent(TcDatePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

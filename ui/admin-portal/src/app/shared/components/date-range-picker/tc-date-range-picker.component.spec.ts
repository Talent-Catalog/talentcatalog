import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDateRangePickerComponent} from './tc-date-range-picker.component';

describe('TcDateRangePickerComponent', () => {
  let component: TcDateRangePickerComponent;
  let fixture: ComponentFixture<TcDateRangePickerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDateRangePickerComponent]
    });
    fixture = TestBed.createComponent(TcDateRangePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

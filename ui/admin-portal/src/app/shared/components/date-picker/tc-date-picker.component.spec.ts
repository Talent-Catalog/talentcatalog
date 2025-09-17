import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDatePickerComponent} from './tc-date-picker.component';
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";

describe('TcDatePickerComponent', () => {
  let component: TcDatePickerComponent;
  let fixture: ComponentFixture<TcDatePickerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDatePickerComponent],
      imports: [NgbDatepickerModule]
    });
    fixture = TestBed.createComponent(TcDatePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

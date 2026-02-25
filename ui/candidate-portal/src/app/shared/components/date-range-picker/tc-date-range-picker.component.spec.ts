import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgbDatepickerModule} from '@ng-bootstrap/ng-bootstrap';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {LanguageService} from "../../../services/language.service";
import {of} from "rxjs";

import {TcDateRangePickerComponent} from './tc-date-range-picker.component';

describe('TcDateRangePickerComponent', () => {
  let component: TcDateRangePickerComponent;
  let fixture: ComponentFixture<TcDateRangePickerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDateRangePickerComponent],
      imports: [NgbDatepickerModule],
      providers: [
        {
          provide: LanguageService,
          useValue: { loadDatePickerLanguageData: () => of(null) }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(TcDateRangePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

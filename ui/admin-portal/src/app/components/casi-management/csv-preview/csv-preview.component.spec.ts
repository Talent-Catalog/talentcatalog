import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {CsvPreviewComponent} from './csv-preview.component';

describe('CsvPreviewComponent', () => {
  let component: CsvPreviewComponent;
  let fixture: ComponentFixture<CsvPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CsvPreviewComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CsvPreviewComponent);
    component = fixture.componentInstance;
    component.csvHeaders = ['A', 'B'];
    component.csvData = [['1', '2']];
    component.paginatedData = [['1', '2']];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('emits pageChange when page changes', () => {
    spyOn(component.pageChange, 'emit');
    component.onPageChanged(2);
    expect(component.pageChange.emit).toHaveBeenCalledWith(2);
  });
});

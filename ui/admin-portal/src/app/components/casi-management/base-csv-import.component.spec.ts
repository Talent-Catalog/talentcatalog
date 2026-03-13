import {BaseCsvImportComponent} from './base-csv-import.component';

class TestCsvImportComponent extends BaseCsvImportComponent {
  requiredColumns = ['colA', 'colB'];
  imported = false;
  loaded = false;

  protected doImport(): void {
    this.imported = true;
    this.working = false;
  }

  protected loadAvailableCount(): void {
    this.loaded = true;
  }
}

describe('BaseCsvImportComponent', () => {
  let component: TestCsvImportComponent;

  beforeEach(() => {
    component = new TestCsvImportComponent();
  });

  it('calls loadAvailableCount on init', () => {
    component.ngOnInit();
    expect(component.loaded).toBeTrue();
  });

  it('sets error when importing without file', () => {
    component.selectedFile = null;
    component.importCSV();
    expect(component.error).toBe('Please select a file to import.');
  });

  it('calls doImport when file is selected', () => {
    component.selectedFile = new File(['colA,colB\n1,2'], 'data.csv');
    component.importCSV();
    expect(component.imported).toBeTrue();
  });

  it('updates paginatedData on page change', () => {
    component.csvData = Array.from({length: 100}, (_, i) => [`row-${i + 1}`]);
    component.pageSize = 30;
    component.onPageChange(2);
    expect(component.currentPage).toBe(2);
    expect(component.paginatedData.length).toBe(30);
    expect(component.paginatedData[0]).toEqual(['row-31']);
  });
});

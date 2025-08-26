import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcTableComponent} from './tc-table.component';

describe('TcTableComponent', () => {
  let component: TcTableComponent;
  let fixture: ComponentFixture<TcTableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcTableComponent]
    });
    fixture = TestBed.createComponent(TcTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display table name when provided', () => {
    component.name = 'Table Name';
    fixture.detectChanges();
    const el = fixture.nativeElement.querySelector('.table-name');
    expect(el.textContent).toContain('Table Name');
  });

  it('should not render table name when name is not provided', () => {
    component.name = undefined;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.table-name')).toBeNull();
  });

  it('should emit correct events on page change', () => {
    spyOn(component.pageNumberChange, 'emit');
    spyOn(component.pageChange, 'emit');
    component.onPageChange(4);
    expect(component.pageNumber).toBe(4);
    expect(component.pageNumberChange.emit).toHaveBeenCalledWith(4);
    expect(component.pageChange.emit).toHaveBeenCalled();
  });

  it('should only show pagination when totalElements > 0', () => {
    component.totalElements = 0;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('tc-pagination')).toBeNull();

    component.totalElements = 5;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('tc-pagination')).not.toBeNull();
  });
});

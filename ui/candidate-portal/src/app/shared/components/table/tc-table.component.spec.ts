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
});

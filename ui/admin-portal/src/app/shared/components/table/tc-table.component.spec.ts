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
});

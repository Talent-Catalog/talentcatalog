import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DisplayTableComponent} from './display-table.component';

describe('TcTableComponent', () => {
  let component: DisplayTableComponent;
  let fixture: ComponentFixture<DisplayTableComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DisplayTableComponent]
    });
    fixture = TestBed.createComponent(DisplayTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

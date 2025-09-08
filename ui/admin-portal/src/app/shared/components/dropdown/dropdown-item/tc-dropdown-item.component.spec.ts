import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDropdownItemComponent} from './tc-dropdown-item.component';

describe('TcDropdownItemComponent', () => {
  let component: TcDropdownItemComponent;
  let fixture: ComponentFixture<TcDropdownItemComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDropdownItemComponent]
    });
    fixture = TestBed.createComponent(TcDropdownItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

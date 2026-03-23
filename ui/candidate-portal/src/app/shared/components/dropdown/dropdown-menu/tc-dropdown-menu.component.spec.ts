import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDropdownMenuComponent} from './tc-dropdown-menu.component';

describe('TcDropdownMenuComponent', () => {
  let component: TcDropdownMenuComponent;
  let fixture: ComponentFixture<TcDropdownMenuComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDropdownMenuComponent]
    });
    fixture = TestBed.createComponent(TcDropdownMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

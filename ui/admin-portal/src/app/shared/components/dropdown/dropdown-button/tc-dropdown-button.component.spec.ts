import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDropdownButtonComponent} from './tc-dropdown-button.component';

describe('TcDropdownButtonComponent', () => {
  let component: TcDropdownButtonComponent;
  let fixture: ComponentFixture<TcDropdownButtonComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDropdownButtonComponent]
    });
    fixture = TestBed.createComponent(TcDropdownButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

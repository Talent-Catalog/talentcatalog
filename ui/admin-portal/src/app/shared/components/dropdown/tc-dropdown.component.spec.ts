import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcDropdownComponent} from './tc-dropdown.component';

describe('DropdownComponent', () => {
  let component: TcDropdownComponent;
  let fixture: ComponentFixture<TcDropdownComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDropdownComponent]
    });
    fixture = TestBed.createComponent(TcDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

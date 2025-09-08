import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DropdownButtonComponent} from './tc-dropdown-button.component';

describe('DropdownButtonComponent', () => {
  let component: DropdownButtonComponent;
  let fixture: ComponentFixture<DropdownButtonComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DropdownButtonComponent]
    });
    fixture = TestBed.createComponent(DropdownButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

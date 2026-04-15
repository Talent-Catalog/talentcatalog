import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TcIconComponent } from './tc-icon.component';

describe('TcIconComponent', () => {
  let component: TcIconComponent;
  let fixture: ComponentFixture<TcIconComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcIconComponent]
    });
    fixture = TestBed.createComponent(TcIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

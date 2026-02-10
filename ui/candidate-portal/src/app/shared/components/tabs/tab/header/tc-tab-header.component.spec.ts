import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcTabHeaderComponent} from './tc-tab-header.component';

describe('TcTabHeaderComponent', () => {
  let component: TcTabHeaderComponent;
  let fixture: ComponentFixture<TcTabHeaderComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcTabHeaderComponent]
    });
    fixture = TestBed.createComponent(TcTabHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

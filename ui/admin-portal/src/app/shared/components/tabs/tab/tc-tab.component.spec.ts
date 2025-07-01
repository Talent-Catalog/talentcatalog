import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcTabComponent} from './tc-tab.component';

describe('TcTabComponent', () => {
  let component: TcTabComponent;
  let fixture: ComponentFixture<TcTabComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcTabComponent]
    });
    fixture = TestBed.createComponent(TcTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

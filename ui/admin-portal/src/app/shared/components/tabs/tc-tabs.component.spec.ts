import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcTabsComponent} from './tc-tabs.component';

describe('TcTabsComponent', () => {
  let component: TcTabsComponent;
  let fixture: ComponentFixture<TcTabsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcTabsComponent]
    });
    fixture = TestBed.createComponent(TcTabsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

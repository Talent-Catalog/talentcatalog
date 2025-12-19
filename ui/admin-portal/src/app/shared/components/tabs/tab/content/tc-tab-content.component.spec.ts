import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcTabContentComponent} from './tc-tab-content.component';

describe('TcTabContentComponent', () => {
  let component: TcTabContentComponent;
  let fixture: ComponentFixture<TcTabContentComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcTabContentComponent]
    });
    fixture = TestBed.createComponent(TcTabContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

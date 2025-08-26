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
    component.id = 'FirstTab';
    component.description = 'This is the first tab description';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have correct id and description', () => {
    expect(component.id).toBe('FirstTab');
    expect(component.description).toBe('This is the first tab description');
  });
});

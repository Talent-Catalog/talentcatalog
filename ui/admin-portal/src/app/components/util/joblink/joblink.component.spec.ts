import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {JoblinkComponent} from './joblink.component';

describe('JoblinkComponent', () => {
  let component: JoblinkComponent;
  let fixture: ComponentFixture<JoblinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JoblinkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JoblinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

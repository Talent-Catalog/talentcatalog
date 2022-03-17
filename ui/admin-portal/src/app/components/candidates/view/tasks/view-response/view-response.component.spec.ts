import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewResponseComponent} from './view-response.component';

describe('ViewResponseComponent', () => {
  let component: ViewResponseComponent;
  let fixture: ComponentFixture<ViewResponseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewResponseComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewResponseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

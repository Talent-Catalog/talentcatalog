import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CitizenshipsComponent} from './citizenships.component';

describe('CitizenshipsComponent', () => {
  let component: CitizenshipsComponent;
  let fixture: ComponentFixture<CitizenshipsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CitizenshipsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CitizenshipsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

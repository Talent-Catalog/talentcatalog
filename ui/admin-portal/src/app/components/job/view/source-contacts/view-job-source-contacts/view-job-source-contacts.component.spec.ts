import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewJobSourceContactsComponent} from './view-job-source-contacts.component';

describe('ViewJobSourceContactsComponent', () => {
  let component: ViewJobSourceContactsComponent;
  let fixture: ComponentFixture<ViewJobSourceContactsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewJobSourceContactsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSourceContactsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

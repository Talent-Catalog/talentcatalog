import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteOccupationComponent } from './delete-occupation.component';

describe('DeleteOccupationComponent', () => {
  let component: DeleteOccupationComponent;
  let fixture: ComponentFixture<DeleteOccupationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeleteOccupationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteOccupationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

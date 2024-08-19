import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DestinationFamilyComponent} from './destination-family.component';

describe('DestinationFamilyComponent', () => {
  let component: DestinationFamilyComponent;
  let fixture: ComponentFixture<DestinationFamilyComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DestinationFamilyComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationFamilyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

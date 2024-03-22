import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ArrestImprisonComponent} from './arrest-imprison.component';

describe('ArrestImprisonComponent', () => {
  let component: ArrestImprisonComponent;
  let fixture: ComponentFixture<ArrestImprisonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ArrestImprisonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArrestImprisonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

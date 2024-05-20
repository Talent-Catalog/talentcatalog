import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SfJoblinkComponent } from './sf-joblink.component';

describe('SfJoblinkComponent', () => {
  let component: SfJoblinkComponent;
  let fixture: ComponentFixture<SfJoblinkComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SfJoblinkComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SfJoblinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

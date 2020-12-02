import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {VisaFourNineFourComponent} from './visa-four-nine-four.component';

describe('VisaFourNineFourComponent', () => {
  let component: VisaFourNineFourComponent;
  let fixture: ComponentFixture<VisaFourNineFourComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VisaFourNineFourComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaFourNineFourComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

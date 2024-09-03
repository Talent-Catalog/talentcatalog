import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JoiComponentComponent} from './joi-component.component';

describe('JoiComponentComponent', () => {
  let component: JoiComponentComponent;
  let fixture: ComponentFixture<JoiComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JoiComponentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JoiComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

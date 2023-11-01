import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateUpdateChatComponent} from './create-update-chat.component';

describe('CreateUpdateChatComponent', () => {
  let component: CreateUpdateChatComponent;
  let fixture: ComponentFixture<CreateUpdateChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateUpdateChatComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdateChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

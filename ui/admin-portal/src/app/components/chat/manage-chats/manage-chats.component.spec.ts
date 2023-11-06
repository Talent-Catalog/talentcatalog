import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ManageChatsComponent} from './manage-chats.component';

describe('ManageChatsComponent', () => {
  let component: ManageChatsComponent;
  let fixture: ComponentFixture<ManageChatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManageChatsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManageChatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, Input, SimpleChange, SimpleChanges} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';

import {ChatsComponent} from './chats.component';
import {JobChat} from '../../../model/chat';

@Component({
  selector: 'app-view-chat',
  template: ''
})
class ViewChatStubComponent {
  @Input() chat: JobChat;
}

describe('ChatsComponent', () => {
  let component: ChatsComponent;
  let fixture: ComponentFixture<ChatsComponent>;

  const firstChat = {
    id: 1
  } as JobChat;

  const secondChat = {
    id: 2
  } as JobChat;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ChatsComponent,
        ViewChatStubComponent
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize without selecting a chat when chats are undefined', () => {
    const selectCurrentSpy = spyOn(component, 'selectCurrent');

    component.chats = undefined;

    component.ngOnChanges({
      chats: new SimpleChange(undefined, undefined, true)
    });

    expect(selectCurrentSpy).not.toHaveBeenCalled();
    expect(component.currentChat).toBeUndefined();
  });

  it('should not select a chat when the chats array is empty', () => {
    const selectCurrentSpy = spyOn(component, 'selectCurrent');

    component.chats = [];

    component.ngOnChanges({
      chats: new SimpleChange(undefined, [], true)
    });

    expect(selectCurrentSpy).not.toHaveBeenCalled();
    expect(component.currentChat).toBeUndefined();
  });

  it('should select the first chat when chats change', () => {
    const selectCurrentSpy = spyOn(component, 'selectCurrent');

    component.chats = [firstChat, secondChat];

    const changes: SimpleChanges = {
      chats: new SimpleChange(
        undefined,
        component.chats,
        true
      )
    };

    component.ngOnChanges(changes);

    expect(selectCurrentSpy).toHaveBeenCalledTimes(1);
    expect(selectCurrentSpy).toHaveBeenCalledWith(firstChat);
  });

  it('should set the first chat as current when chats change', () => {
    const emitSpy = spyOn(component.chatSelection, 'emit');

    component.chats = [firstChat, secondChat];

    component.ngOnChanges({
      chats: new SimpleChange(
        undefined,
        component.chats,
        true
      )
    });

    expect(component.currentChat).toBe(firstChat);
    expect(emitSpy).toHaveBeenCalledTimes(1);
    expect(emitSpy).toHaveBeenCalledWith(firstChat);
  });

  it('should set the selected chat as the current chat', () => {
    component.selectCurrent(secondChat);

    expect(component.currentChat).toBe(secondChat);
  });

  it('should emit the selected chat', () => {
    const emitSpy = spyOn(component.chatSelection, 'emit');

    component.selectCurrent(secondChat);

    expect(emitSpy).toHaveBeenCalledTimes(1);
    expect(emitSpy).toHaveBeenCalledWith(secondChat);
  });

  it('should replace the current chat when another chat is selected', () => {
    component.selectCurrent(firstChat);

    expect(component.currentChat).toBe(firstChat);

    component.selectCurrent(secondChat);

    expect(component.currentChat).toBe(secondChat);
  });

  it('should render one list item for each chat', () => {
    component.chats = [firstChat, secondChat];
    fixture.detectChanges();

    const chatItems = fixture.debugElement.queryAll(
      By.css('li.list-group-item')
    );

    expect(chatItems.length).toBe(2);
  });

  it('should pass each chat to its view-chat component', () => {
    component.chats = [firstChat, secondChat];
    fixture.detectChanges();

    const viewChatComponents = fixture.debugElement.queryAll(
      By.directive(ViewChatStubComponent)
    );

    expect(viewChatComponents.length).toBe(2);

    expect(
      (viewChatComponents[0].componentInstance as ViewChatStubComponent).chat
    ).toBe(firstChat);

    expect(
      (viewChatComponents[1].componentInstance as ViewChatStubComponent).chat
    ).toBe(secondChat);
  });

  it('should select and emit a chat when its list item is clicked', () => {
    const emitSpy = spyOn(component.chatSelection, 'emit');

    component.chats = [firstChat, secondChat];
    fixture.detectChanges();

    const chatItems = fixture.debugElement.queryAll(
      By.css('li.list-group-item')
    );

    chatItems[1].triggerEventHandler('click', null);

    expect(component.currentChat).toBe(secondChat);
    expect(emitSpy).toHaveBeenCalledTimes(1);
    expect(emitSpy).toHaveBeenCalledWith(secondChat);
  });

  it('should add the active class to the current chat', () => {
    component.chats = [firstChat, secondChat];
    component.currentChat = secondChat;
    fixture.detectChanges();

    const chatItems = fixture.debugElement.queryAll(
      By.css('li.list-group-item')
    );

    expect(chatItems[0].classes['bg-active-custom']).toBeFalsy();
    expect(chatItems[1].classes['bg-active-custom']).toBeTrue();
  });

  it('should update the active class when a different chat is clicked', () => {
    component.chats = [firstChat, secondChat];
    component.currentChat = firstChat;
    fixture.detectChanges();

    let chatItems = fixture.debugElement.queryAll(
      By.css('li.list-group-item')
    );

    expect(chatItems[0].classes['bg-active-custom']).toBeTrue();
    expect(chatItems[1].classes['bg-active-custom']).toBeFalsy();

    chatItems[1].triggerEventHandler('click', null);
    fixture.detectChanges();

    chatItems = fixture.debugElement.queryAll(
      By.css('li.list-group-item')
    );

    expect(chatItems[0].classes['bg-active-custom']).toBeFalsy();
    expect(chatItems[1].classes['bg-active-custom']).toBeTrue();
  });

  it('should display the loading indicator while loading', () => {
    component.loading = true;
    fixture.detectChanges();

    const loadingIndicator = fixture.debugElement.query(
      By.css('.fa-spinner')
    );

    expect(loadingIndicator).not.toBeNull();
    expect(loadingIndicator.nativeElement.parentElement.textContent)
    .toContain('loading...');
  });

  it('should hide the loading indicator when not loading', () => {
    component.loading = false;
    fixture.detectChanges();

    const loadingIndicator = fixture.debugElement.query(
      By.css('.fa-spinner')
    );

    expect(loadingIndicator).toBeNull();
  });

  it('should display an error message when an error exists', () => {
    component.error = 'Unable to load chats';
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(
      By.css('.alert-danger')
    );

    expect(errorElement).not.toBeNull();
    expect(errorElement.nativeElement.textContent)
    .toContain('Unable to load chats');
  });

  it('should hide the error message when there is no error', () => {
    component.error = null;
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(
      By.css('.alert-danger')
    );

    expect(errorElement).toBeNull();
  });

  it('should render no chat items when the chats array is empty', () => {
    component.chats = [];
    fixture.detectChanges();

    const chatItems = fixture.debugElement.queryAll(
      By.css('li.list-group-item')
    );

    expect(chatItems.length).toBe(0);
  });
});

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

import {CommonModule} from '@angular/common';
import {QueryList} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {RouterTestingModule} from '@angular/router/testing';

import {TcAccordionItemComponent} from './accordion-item/tc-accordion-item.component';
import {TcAccordionComponent} from './tc-accordion.component';

describe('TcAccordionComponent', () => {
  let component: TcAccordionComponent;

  beforeEach(() => {
    component = new TcAccordionComponent();
    component.items = createItems(3);
  });

  it('should create with the default values', () => {
    expect(component).toBeTruthy();
    expect(component.allOpen).toBeFalse();
    expect(component.firstOpen).toBeFalse();
    expect(component.showOpenCloseAll).toBeTrue();
    expect(component.allowOverflow).toBeFalse();
    expect(component.activeIndexes).toEqual([]);
  });

  it('should set multiple active indexes', () => {
    component.activeIndexes = [1, 3, 5];

    expect(component.activeIndexes).toEqual([1, 3, 5]);
    expect(component.isOpen(1)).toBeTrue();
    expect(component.isOpen(3)).toBeTrue();
    expect(component.isOpen(5)).toBeTrue();
  });

  it('should set a single active index', () => {
    component.activeIndexes = 2;

    expect(component.activeIndexes).toEqual([2]);
    expect(component.isOpen(2)).toBeTrue();
  });

  it('should replace previously active indexes', () => {
    component.activeIndexes = [0, 1, 2];

    expect(component.activeIndexes).toEqual([0, 1, 2]);

    component.activeIndexes = [4, 5];

    expect(component.activeIndexes).toEqual([4, 5]);
    expect(component.isOpen(0)).toBeFalse();
    expect(component.isOpen(1)).toBeFalse();
    expect(component.isOpen(2)).toBeFalse();
    expect(component.isOpen(4)).toBeTrue();
    expect(component.isOpen(5)).toBeTrue();
  });

  it('should remove duplicate active indexes', () => {
    component.activeIndexes = [1, 1, 2, 2];

    expect(component.activeIndexes).toEqual([1, 2]);
  });

  it('should clear active indexes when null is supplied', () => {
    component.activeIndexes = [0, 1];

    expect(component.activeIndexes).toEqual([0, 1]);

    component.activeIndexes = null;

    expect(component.activeIndexes).toEqual([]);
    expect(component.isOpen(0)).toBeFalse();
    expect(component.isOpen(1)).toBeFalse();
  });

  it('should clear active indexes when undefined is supplied', () => {
    component.activeIndexes = [0, 1];

    expect(component.activeIndexes).toEqual([0, 1]);

    component.activeIndexes = undefined;

    expect(component.activeIndexes).toEqual([]);
    expect(component.isOpen(0)).toBeFalse();
    expect(component.isOpen(1)).toBeFalse();
  });

  it('should assign indexes to child items', () => {
    component.ngAfterContentInit();

    expect(getItems(component)[0].index).toBe(0);
    expect(getItems(component)[1].index).toBe(1);
    expect(getItems(component)[2].index).toBe(2);
  });

  it('should leave all panels closed by default', () => {
    component.ngAfterContentInit();

    expect(component.activeIndexes).toEqual([]);
  });

  it('should open all panels when allOpen is true', () => {
    component.allOpen = true;

    component.ngAfterContentInit();

    expect(component.activeIndexes).toEqual([0, 1, 2]);
  });

  it('should open the first panel when firstOpen is true', () => {
    component.firstOpen = true;

    component.ngAfterContentInit();

    expect(component.activeIndexes).toEqual([0]);
  });

  it('should give allOpen priority over firstOpen', () => {
    component.allOpen = true;
    component.firstOpen = true;

    component.ngAfterContentInit();

    expect(component.activeIndexes).toEqual([0, 1, 2]);
  });

  it('should close an open panel and emit the new indexes', () => {
    component.activeIndexes = [0, 1];

    const emitSpy = spyOn(
      component.activeIndexesChange,
      'emit'
    );

    component.toggle(1);

    expect(component.activeIndexes).toEqual([0]);
    expect(component.isOpen(1)).toBeFalse();
    expect(emitSpy).toHaveBeenCalledOnceWith([0]);
  });

  it('should open a closed panel and emit the new indexes', () => {
    component.activeIndexes = [0];

    const emitSpy = spyOn(
      component.activeIndexesChange,
      'emit'
    );

    component.toggle(2);

    expect(component.activeIndexes).toEqual([0, 2]);
    expect(component.isOpen(2)).toBeTrue();
    expect(emitSpy).toHaveBeenCalledOnceWith([0, 2]);
  });

  it('should report whether a panel is open', () => {
    component.activeIndexes = [1];

    expect(component.isOpen(1)).toBeTrue();
    expect(component.isOpen(0)).toBeFalse();
  });

  it('should open all panels and emit their indexes', () => {
    component.ngAfterContentInit();

    const emitSpy = spyOn(
      component.activeIndexesChange,
      'emit'
    );

    component.openAll();

    expect(component.activeIndexes).toEqual([0, 1, 2]);

    expect(emitSpy).toHaveBeenCalledOnceWith(
      [0, 1, 2]
    );
  });

  it('should close all panels and emit an empty array', () => {
    component.ngAfterContentInit();
    component.activeIndexes = [0, 1, 2];

    const emitSpy = spyOn(
      component.activeIndexesChange,
      'emit'
    );

    component.closeAll();

    expect(component.activeIndexes).toEqual([]);
    expect(emitSpy).toHaveBeenCalledOnceWith([]);
  });

  it('should open all panels when toggleAll is called with none open', () => {
    component.ngAfterContentInit();

    const emitSpy = spyOn(
      component.activeIndexesChange,
      'emit'
    );

    component.toggleAll();

    expect(component.activeIndexes).toEqual([0, 1, 2]);

    expect(emitSpy).toHaveBeenCalledOnceWith(
      [0, 1, 2]
    );
  });

  it('should close all panels when toggleAll is called with panels open', () => {
    component.ngAfterContentInit();
    component.activeIndexes = [1];

    const emitSpy = spyOn(
      component.activeIndexesChange,
      'emit'
    );

    component.toggleAll();

    expect(component.activeIndexes).toEqual([]);
    expect(emitSpy).toHaveBeenCalledOnceWith([]);
  });

  it('should handle an accordion with no child items', () => {
    component.items = createItems(0);

    component.ngAfterContentInit();
    component.openAll();

    expect(component.activeIndexes).toEqual([]);
  });
});

describe('TcAccordionComponent template', () => {
  let fixture: ComponentFixture<TcAccordionComponent>;
  let component: TcAccordionComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, RouterTestingModule, TcAccordionComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(
      TcAccordionComponent
    );

    component = fixture.componentInstance;
  });

  it('should render the open/close-all button by default', () => {
    fixture.detectChanges();

    const button = fixture.debugElement.query(
      By.css('.toggle-all-panels')
    );

    expect(button).toBeTruthy();
    expect(button.nativeElement.textContent.trim())
    .toBe('Open/Close All');
  });

  it('should hide the open/close-all button when disabled', () => {
    component.showOpenCloseAll = false;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(
        By.css('.toggle-all-panels')
      )
    ).toBeNull();
  });

  it('should add the allow-overflow class when enabled', () => {
    component.allowOverflow = true;

    fixture.detectChanges();

    const accordion = fixture.debugElement.query(
      By.css('.tc-accordion')
    );

    expect(
      accordion.nativeElement.classList.contains(
        'allow-overflow'
      )
    ).toBeTrue();
  });

  it('should not add the allow-overflow class by default', () => {
    fixture.detectChanges();

    const accordion = fixture.debugElement.query(
      By.css('.tc-accordion')
    );

    expect(
      accordion.nativeElement.classList.contains(
        'allow-overflow'
      )
    ).toBeFalse();
  });

  it('should call toggleAll when the button emits onClick', () => {
    const toggleAllSpy = spyOn(component, 'toggleAll');

    fixture.detectChanges();

    const button = fixture.debugElement.query(
      By.css('.toggle-all-panels')
    );

    button.triggerEventHandler('onClick', undefined);

    expect(toggleAllSpy).toHaveBeenCalledTimes(1);
  });
});

function createItems(
  count: number
): QueryList<TcAccordionItemComponent> {
  const queryList =
    new QueryList<TcAccordionItemComponent>();

  const items = Array.from(
    {length: count},
    () => ({
      index: -1
    } as TcAccordionItemComponent)
  );

  queryList.reset(items);

  return queryList;
}

function getItems(
  component: TcAccordionComponent
): TcAccordionItemComponent[] {
  return component.items.toArray();
}

/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Component, DebugElement} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LowercaseDirective} from './lowercase.directive';
import {HighlightSearchDirective} from './highlight-search.directive';
import {DirectiveModule} from './directive.module';
import {By} from '@angular/platform-browser';
import {NgControl} from "@angular/forms";
import {Subject} from "rxjs";

@Component({
  template: `
    <div appLowercase></div>
    <div appHighlightSearch class="highlight">Angular</div>
  `
})
class TestComponent {}

describe('DirectiveModule', () => {
  let fixture: ComponentFixture<TestComponent>;
  let debugElement: DebugElement;
  let searchTerms$: Subject<string[]>;
  beforeEach(() => {
    searchTerms$ = new Subject<string[]>();
    TestBed.configureTestingModule({
      declarations: [TestComponent],
      imports: [CommonModule, DirectiveModule],
      providers: [NgControl]
    });

    fixture = TestBed.createComponent(TestComponent);
    debugElement = fixture.debugElement;
    fixture.detectChanges();
  });

  it('should declare LowercaseDirective', () => {
    const lowercaseDirective = debugElement.query(By.directive(LowercaseDirective));
    expect(lowercaseDirective).toBeTruthy();
  });


  it('should declare HighlightSearchDirective', () => {
    const highlightSearchDirective = debugElement.query(By.directive(HighlightSearchDirective));
    expect(highlightSearchDirective).toBeTruthy();
  });


  it('should highlight search terms in the element text content', () => {
    fixture.detectChanges();
    searchTerms$.next(['Angular']);
    fixture.detectChanges();

    const highlightedElements = fixture.nativeElement.querySelectorAll('.highlight');
    expect(highlightedElements.length).toBe(1);
    expect(highlightedElements[0].textContent).toBe('Angular');
  });

});

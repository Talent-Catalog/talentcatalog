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

import {FixedInputComponent} from "./fixed-input.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {By} from "@angular/platform-browser";

describe('FixedInputComponent', () => {
  let component: FixedInputComponent;
  let fixture: ComponentFixture<FixedInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FixedInputComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FixedInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the question', () => {
    component.question = 'What is your name?';
    fixture.detectChanges();

    const questionElement = fixture.debugElement.query(By.css('.question')).nativeElement;
    expect(questionElement.textContent).toContain('What is your name?:');
  });

  it('should display the answer', () => {
    component.answer = 'John Doe';
    fixture.detectChanges();

    const answerElement = fixture.debugElement.query(By.css('.answer')).nativeElement;
    expect(answerElement.textContent).toBe('John Doe');
  });

  it('should convert answer to string', () => {
    component.answer = 12345;
    fixture.detectChanges();

    const answerElement = fixture.debugElement.query(By.css('.answer')).nativeElement;
    expect(answerElement.textContent).toBe('12345');
  });

  it('should not display answer when it is null', () => {
    component.answer = null;
    fixture.detectChanges();

    const answerElement = fixture.debugElement.query(By.css('.answer'));
    expect(answerElement).toBeNull();
  });

});

import {Component, Input} from '@angular/core';
import {TextParts, TextPartsCodec} from "../../../util/text-parts/text-parts";
import {CommonModule} from "@angular/common";

/**
 * Component to display the text parts of a text.
 * <p>
 * Example:
 * <pre>
 *  <app-text-parts-view [text]="candidate.description"></app-text-parts-view>
 * </pre>
 >
 */
@Component({
  selector: 'app-text-parts-view',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './text-parts-view.component.html',
  styleUrl: './text-parts-view.component.scss'
})
export class TextPartsViewComponent {
  @Input() hideKeywords: boolean = false;
  @Input() hideTidied: boolean = false;

  private value?: string | null;

  parts: TextParts = {
    original: '',
    tidied: '',
    keywords: []
  };

  @Input()
  set text(value: string | null | undefined) {
    this.value = value;
    this.parts = TextPartsCodec.read(value);
  }

  get text(): string | null | undefined {
    return this.value;
  }

  hasTidied(): boolean {
    return !!this.parts.tidied?.trim();
  }

  hasKeywords(): boolean {
    return !!this.parts.keywords?.length;
  }
}

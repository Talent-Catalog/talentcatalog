import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Link, URL_REGEX} from "../../model/base";

/**
 * Provides a modal for setting the properties of link-formatted text.
 */
@Component({
  selector: 'app-build-link',
  templateUrl: './build-link.component.html',
  styleUrls: ['./build-link.component.scss']
})
export class BuildLinkComponent implements OnInit {

  form: UntypedFormGroup;
  error: string;
  title: string;

  @Input() placeholder: string;
  @Input() currentUrl: string = '';

  @ViewChild('urlInput') urlInput: ElementRef;

  readonly urlRegex: string = URL_REGEX;

  constructor(private modal: NgbActiveModal,
              private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      placeholder: [this.placeholder, Validators.required],
      url: [this.currentUrl, [Validators.required]]
    });
    this.computeHeader()
  }

  /**
   * If a URL is passed, then we're working with an existing link.
   * @private
   */
  private computeHeader(): void {
    this.title = this.currentUrl ? 'Edit Link' : 'Add Link';
  }

  ngAfterViewInit(): void {
    this.focusInput();
  }

  /**
   * Placeholder will always be pre-filled, so we focus on url input
   */
  focusInput(): void {
    this.urlInput.nativeElement.focus();
  }

  private getUrl() {
    return this.form.value.url;
  }

  private getPlaceholder() {
    return this.form.value.placeholder;
  }

  private createLink(): Link {
    const link: Link = {
      url: this.getUrl(),
      placeholder: this.getPlaceholder()
    }

    return link
  }

  cancel() {
    this.modal.close();
  }

  save() {
    this.modal.close(this.createLink())
  }

  onKeydownEnter(event: any) {
    event.preventDefault(); // Stops newline in editor when form submitted
    if (this.form.valid) {
      this.save()
    }
  }

}

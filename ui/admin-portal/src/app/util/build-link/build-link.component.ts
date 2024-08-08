import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Link, URL_REGEX} from "../../model/base";

@Component({
  selector: 'app-build-link',
  templateUrl: './build-link.component.html',
  styleUrls: ['./build-link.component.scss']
})
export class BuildLinkComponent implements OnInit {

  form: FormGroup;
  error: string;
  title: string = 'Edit Link'

  @Input() selectedText: string;
  @Input() currentUrl: string = '';

  @ViewChild('urlInput') urlInput: ElementRef;

  readonly urlRegex: string = URL_REGEX;

  constructor(private modal: NgbActiveModal,
              private fb: FormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      placeholder: [this.selectedText, Validators.required],
      url: [this.currentUrl, [Validators.required]]
    });
  }

  ngAfterViewInit(): void {
    this.focusInput();
  }

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
    let link: Link = {
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
    event.preventDefault();
    if (this.form.valid) {
      this.save()
    }
  }

}

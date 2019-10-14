import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Language} from "../../../../model/language";
import {LanguageService} from "../../../../services/language.service";
import {LanguageLevel} from "../../../../model/language-level";

@Component({
  selector: 'app-language-level-form-control',
  templateUrl: './language-level-form-control.component.html',
  styleUrls: ['./language-level-form-control.component.scss']
})
export class LanguageLevelFormControlComponent implements OnInit {

  @Input() languages: Language[];
  @Input() languageLevels: LanguageLevel[];

  @Output() change = new EventEmitter();

  showMenu: boolean;

  constructor(private languageService: LanguageService) { }

  ngOnInit() {
    if (!this.languages) {
      this.languageService.listLanguages().subscribe(
        (response) => {
          this.languages = response;
        },
        (error) => {
          console.error(error);
        });
    }
  }

  toggle() {
    this.showMenu = !this.showMenu;
  }

  open() {
    this.showMenu = true;
  }

  close() {
    this.showMenu = false;
  }

  renderProficiency() {
    return 'TODO: Render proficiency';
  }
}

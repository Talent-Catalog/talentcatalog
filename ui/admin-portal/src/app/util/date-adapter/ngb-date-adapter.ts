import {Injectable} from '@angular/core';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';

@Injectable()
export class CustomDateAdapter {
  // String -> NgbDateStruct
  fromModel(value: string): NgbDateStruct {
    if (!value) {
      return null;
    }
    let [y, m, d] = value.toString().split('-');
    return {
      year: +y,
      month: +m,
      day: +d
    } as NgbDateStruct;
  }

  // NgbDateStruct -> String
  toModel(date: NgbDateStruct): string {
    return date ?
      date.year + "-" + ('0' + date.month).slice(-2) + "-" + ('0' + date.day).slice(-2) :
      null;
  }
}

@Injectable()
export class CustomDateParserFormatter {
  // String -> NgbDateStruct
  parse(value: string): NgbDateStruct {
    if (!value) {
      return null;
    }
    let [y, m, d] = value.toString().split('-');
    return {
      year: +y,
      month: +m,
      day: +d
    } as NgbDateStruct;
  }
  // NgbDateStruct -> String
  format(date: NgbDateStruct): string {
    return date ?
      date.year + "-" + ('0' + date.month).slice(-2) + "-" + ('0' + date.day).slice(-2) :
      null;
  }
}


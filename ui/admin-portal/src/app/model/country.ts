import {HasName} from './base';

export interface Country extends HasName {
  id: number;
  name: string;
  status: string;
  translatedName: string;
}

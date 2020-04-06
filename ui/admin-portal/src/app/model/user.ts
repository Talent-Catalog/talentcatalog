import {Country} from "./country";

export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  readOnly: boolean;
  sourceCountries: Country[];
  status: string;
  createdDate: number;
  lastLogin: number;
}



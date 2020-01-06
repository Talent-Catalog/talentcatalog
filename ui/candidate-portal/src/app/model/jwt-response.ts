import {User} from "./user";

export interface JwtResponse {
  accessToken: string;
  name: string;
  gender: string;
  user: User;
}

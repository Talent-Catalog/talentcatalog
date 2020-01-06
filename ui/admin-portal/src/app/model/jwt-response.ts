import {Candidate} from "./candidate";

export interface JwtResponse {
  accessToken: string;
  name: string;
  gender: string;
  user: Candidate;
}

import { Injectable } from "@angular/core";
import { AngularFireMessaging } from "@angular/fire/compat/messaging";
import { tap } from "rxjs/operators";

@Injectable({
  providedIn: "root",
})
export class FcmService {
  token = null;

  constructor(private fireMessaging: AngularFireMessaging) {}

  requestToken() {
    return this.fireMessaging.requestToken.pipe(
      tap((token) => {
        console.log("Store token to server: ", token);
      })
    );
  }

  getMessages() {
    return this.fireMessaging.messages;
  }

  deleteToken() {
    if (this.token) {
      this.fireMessaging.deleteToken(this.token);
      this.token = null;
    }
  }

  // listenForMessages(): void {
  //   this.fireMessaging.messages.subscribe((message) => {
  //     console.log("Foreground message:", message);
  //   });
  // }
}

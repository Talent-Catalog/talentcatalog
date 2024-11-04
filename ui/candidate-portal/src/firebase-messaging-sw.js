importScripts("https://www.gstatic.com/firebasejs/7.18.0/firebase-app.js");
importScripts(
  "https://www.gstatic.com/firebasejs/7.18.0/firebase-messaging.js"
);

firebase.initializeApp({
  projectId: "fortesttccatalog",
  appId: "1:47912608172:web:a2e5df8650c429ada4da09",
  storageBucket: "fortesttccatalog.appspot.com",
  apiKey: "AIzaSyBEaYGOPYET2gcLWjiCTgnTCpQp3MYPr24",
  authDomain: "fortesttccatalog.firebaseapp.com",
  messagingSenderId: "47912608172",
});

const messaging = firebase.messaging();

// messaging.onBackgroundMessage((payload) => {
//   console.log(
//     "[firebase-messaging-sw.js] Received background message ",
//     payload
//   );
//   const notificationTitle = "Background Message Title";
//   const notificationOptions = {
//     body: "Background Message body.",
//     icon: "/firebase-logo.png",
//   };

//   self.registration.showNotification(notificationTitle, notificationOptions);
// });

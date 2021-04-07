import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!"); //sends back to the client
// });

admin.initializeApp();

export const notificationForChallenge = functions.firestore
    .document("/challenges/{id}")
    .onCreate(async snapshot => {
      const challenge = snapshot.data();
      // send a notification to the chat receiver
      const payload: admin.messaging.MessagingPayload = {
        notification: {
          title: `Push Sent From ${challenge.senderUsername}`,
          body: `Press 'accept' to approve`,
		  click_action: `android.intent.action.MAIN`
        },
        //add data
		data: {
		  approved: `true`,
		  otherUserId: challenge.senderId,
		  otherUserDeviceToken: challenge.senderDeviceToken,
		  otherUserUsername: challenge.senderUsername,
		  chatId: challenge.chatId
		}
        }
      return admin.messaging().sendToDevice(challenge.receiverDeviceToken, payload);
      });
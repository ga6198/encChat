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
    .document("/challenges/{challengeId}")
    .onCreate(async (snapshot, context) => {
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
		  challenge: challenge.challenge, 
		  challengeId: context.params.challengeId,
		  approved: `true`,
		  senderId: challenge.senderId,
		  senderDeviceToken: challenge.senderDeviceToken,
		  senderUsername: challenge.senderUsername,
		  receiverId: challenge.receiverId,
		  receiverUsername: challenge.receiverUsername,
		  receiverDeviceToken: challenge.receiverDeviceToken,
		  chatId: challenge.chatId,
		  time: challenge.time.seconds.toString()
		}
        }
      return admin.messaging().sendToDevice(challenge.receiverDeviceToken, payload);
      });
	  
export const notificationForChallengeResponse = functions.firestore
    .document("/challenges/{challengeId}")
    .onUpdate(async (change, context) => {
      const challenge = change.after.data();
      // send a notification to the original challenge sender
	  if(challenge.approved == true){
		  const payload: admin.messaging.MessagingPayload = {
			notification: {
			  title: `Push challenge approved`,
			  body: `${challenge.receiverUsername} responded to your push`,
			  click_action: `android.intent.action.MAIN`
			},
		    data:{
			  challengeResponse: `true`,
			  chatId: challenge.chatId,
			  time: challenge.time.seconds.toString(),
			  challengeResponseData: challenge.challengeResponseData,
			  senderUsername: challenge.receiverUsername, //the sender of the response is the person who received the first challenge
			  receiverId: challenge.senderId //the receiver is the person who first sent the challenge
			}
			}
		  return admin.messaging().sendToDevice(challenge.senderDeviceToken, payload);
      }
	  return null;
	  });

export const notificationForChatMessage = functions.firestore
	.document("/chats/{chatId}/messages/{messageId}")
	.onCreate(async (snapshot, context) => {
	  const message = snapshot.data();
	  
	  //time of message
	  //var d = new Date(message.time)
	  //var time = d.getHours() + ":" + d.getMinutes()
	  
      // send a notification to the chat receiver
	  const payload: admin.messaging.MessagingPayload = {
        notification: {
          title: `Message from ${message.senderUsername}`,
          body: `Open the app to view`,
		  click_action: `android.intent.action.MAIN`
        },
        }
      return admin.messaging().sendToDevice(message.receiverDeviceToken, payload);
	})
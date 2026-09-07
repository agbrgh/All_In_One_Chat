const {onSchedule} = require("firebase-functions/v2/scheduler");
const {onRequest} = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();
const db = admin.firestore();

// 1. 5분마다 작동하는 디스코드 메시지 스케줄러
exports.checkDiscordMessages = onSchedule({
  schedule: "every 5 minutes",
  region: "asia-northeast3",
}, async (event) => {
  try {
    const usersSnapshot = await db.collection("users")
        .where("discordLinked", "==", true).get();

    if (usersSnapshot.empty) return;

    for (const userDoc of usersSnapshot.docs) {
      const userData = userDoc.data();
      const accessToken = userData.discordToken;
      const channels = userData.targetChannels || [];

      for (const channelId of channels) {
        const lastMessageId = userData[`last_msg_${channelId}`] || null;
        let url = `https://discord.com{channelId}/messages?limit=10`;
        if (lastMessageId) {
          url += `&after=${lastMessageId}`;
        }

        try {
          const response = await axios.get(url, {
            headers: {Authorization: `Bearer ${accessToken}`},
          });

          const messages = response.data;
          if (messages.length > 0) {
            const newestMessageId = messages.id;
            const batch = db.batch();

            messages.forEach((msg) => {
              const msgRef = db.collection("channels").document(channelId)
                  .collection("messages").document(msg.id);
              batch.set(msgRef, {
                sender: msg.author.username,
                content: msg.content,
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                discordCreatedAt: msg.timestamp,
              });
            });

            const userRef = db.collection("users").document(userDoc.id);
            batch.update(userRef, {
              [`last_msg_${channelId}`]: newestMessageId,
            });

            await batch.commit();
          }
        } catch (apiError) {
          if (apiError.response && apiError.response.status === 401) {
            console.error("Token expired for user:", userDoc.id);
          }
        }
      }
    }
  } catch (error) {
    console.error("Scheduler error:", error);
  }
});

// 2. 안드로이드 앱에서 일회용 코드를 받아 진짜 토큰으로 교환하는 함수
// 💡 디스코드 개발자 포털에서 복사한 값으로 정확히 변경해 주세요!
const DISCORD_CLIENT_ID = "1541347865308299294";
const DISCORD_CLIENT_SECRET = "e_pGhFaOLATmi8aNHizP9otfEq8OqyPw";
const REDIRECT_URI = "all-in-one-chat://discord-login";

exports.exchangeDiscordToken = onRequest({
  region: "asia-northeast3",
}, async (req, res) => {
  const {code, userId} = req.body;

  if (!code || !userId) {
    return res.status(400).send("Missing code or userId");
  }

  const params = new URLSearchParams();
  params.append("client_id", DISCORD_CLIENT_ID);
  params.append("client_secret", DISCORD_CLIENT_SECRET);
  params.append("grant_type", "authorization_code");
  params.append("code", code);
  params.append("redirect_uri", REDIRECT_URI);

  try {
    const response = await axios.post(
        "https://discord.com",
        params,
        {headers: {"Content-Type": "application/x-www-form-urlencoded"}},
    );

    const tokenData = response.data;
    const accessToken = tokenData.access_token;

    await db.collection("users").document(userId).set({
      discordLinked: true,
      discordToken: accessToken,
      targetChannels: ["여기에_테스트할_디스코드_채널ID_입력"],
    }, {merge: true});

    return res.status(200).json({status: "success"});
  } catch (error) {
    // 💡 문제가 되었던 부분을 구식 엔진 호환 방식으로 수정했습니다.
    if (error.response) {
      console.error("Token exchange error data:", error.response.data);
    } else {
      console.error("Token exchange error message:", error.message);
    }
    return res.status(500).send("Failed to exchange token");
  }
});


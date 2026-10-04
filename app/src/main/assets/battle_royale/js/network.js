/* ═══════════════════════════════════════════════
   Network Layer — Firebase Realtime Database
   ═══════════════════════════════════════════════ */

const Network = {
  db: null,
  auth: null,
  user: null,
  userId: null,
  userName: '',
  currentMatchId: null,
  matchRef: null,
  listeners: {},
  
  // Sync rate (Hz)
  SYNC_RATE: 5, // 5 updates/second
  lastSync: 0
};

// ═══ Firebase Config ═══
const FIREBASE_CONFIG = {
  apiKey: "AIzaSyCyYnCpD4kyywObEW39jkY8qKzbqi5Z0Eo",
  authDomain: "ummah-15bad.firebaseapp.com",
  databaseURL: "https://ummah-15bad-default-rtdb.firebaseio.com/",
  projectId: "ummah-15bad",
  storageBucket: "ummah-15bad.appspot.com",
  messagingSenderId: "462453476103",
  appId: "1:462453476103:android:20a714a864d71d5f7a13a2"
};

// ═══ INIT ═══
async function initNetwork() {
  try {
    firebase.initializeApp(FIREBASE_CONFIG);
    Network.auth = firebase.auth();
    Network.db = firebase.database();

    // Anonymous auth
    const cred = await Network.auth.signInAnonymously();
    Network.user = cred.user;
    Network.userId = cred.user.uid;

    // Load user profile
    if (window.Android && window.Android.getPlayerId) {
      Network.userName = window.Android.getPlayerName() || 'عميل';
      const playerId = window.Android.getPlayerId();
      if (playerId) {
        // Use player national ID
        Network.userId = playerId;
      }
    }

    console.log('✅ Network ready. User:', Network.userId, Network.userName);
    updateLoaderStatus('متصل ✓');
    return true;
  } catch (e) {
    console.error('Network error:', e);
    updateLoaderStatus('❌ فشل الاتصال: ' + e.message);
    return false;
  }
}

function updateLoaderStatus(text) {
  const el = document.getElementById('loader-status');
  if (el) el.textContent = text;
}

// ═══ MATCHMAKING ═══
Network.matchQueueRef = () => Network.db.ref('battle_royale/queue');

Network.joinQueue = async function() {
  const queueRef = Network.matchQueueRef().child(Network.userId);
  await queueRef.set({
    userId: Network.userId,
    userName: Network.userName,
    joinedAt: firebase.database.ServerValue.TIMESTAMP,
    status: 'waiting'
  });
  console.log('✅ Joined queue');
};

Network.leaveQueue = async function() {
  if (Network.userId) {
    await Network.matchQueueRef().child(Network.userId).remove();
  }
};

// ═══ MATCH LISTENER ═══
Network.listenQueue = function(callback) {
  Network.matchQueueRef().on('value', snapshot => {
    const players = [];
    snapshot.forEach(child => {
      players.push(child.val());
    });
    callback(players);
  });
};

// ═══ FIND MATCH — Server-side function (via Cloud Function or manual) ═══
// For now, clients check for open matches
Network.findOrCreateMatch = async function() {
  const matchesRef = Network.db.ref('battle_royale/matches');
  const snap = await matchesRef.orderByChild('status').equalTo('waiting').limitToFirst(1).get();
  
  let matchId;
  if (snap.exists()) {
    // Join existing
    const matches = snap.val();
    matchId = Object.keys(matches)[0];
    await joinMatch(matchId);
  } else {
    // Create new
    matchId = 'match_' + Date.now();
    await matchesRef.child(matchId).set({
      status: 'waiting',
      createdAt: firebase.database.ServerValue.TIMESTAMP,
      playerCount: 0,
      phase: 1,
      circleX: 1200,
      circleY: 800,
      circleR: 1200,
      maxPlayers: 10
    });
    await joinMatch(matchId);
  }
  
  return matchId;
};

async function joinMatch(matchId) {
  const matchRef = Network.db.ref('battle_royale/matches/' + matchId);
  const playerRef = matchRef.child('players/' + Network.userId);
  
  await playerRef.set({
    userId: Network.userId,
    userName: Network.userName,
    x: 200 + Math.random() * 400,
    y: 200 + Math.random() * 400,
    hp: 100,
    ammo: 30,
    weapon: 'pistol',
    alive: true,
    kills: 0,
    joinedAt: firebase.database.ServerValue.TIMESTAMP,
    lastUpdate: firebase.database.ServerValue.TIMESTAMP
  });
  
  // Increment player count
  await matchRef.child('playerCount').transaction(c => (c || 0) + 1);
  
  Network.currentMatchId = matchId;
  Network.matchRef = matchRef;
  
  console.log('✅ Joined match:', matchId);
}

// ═══ POSITION SYNC ═══
Network.syncPosition = function(player) {
  const now = performance.now();
  if (now - Network.lastSync < 1000 / Network.SYNC_RATE) return;
  Network.lastSync = now;
  
  if (!Network.matchRef) return;
  
  Network.matchRef.child('players/' + Network.userId).update({
    x: Math.round(player.x),
    y: Math.round(player.y),
    aimAngle: Math.round(player.aimAngle * 100) / 100,
    hp: player.hp,
    ammo: player.ammo,
    weapon: player.weaponKey,
    alive: player.alive,
    kills: player.kills,
    lastUpdate: firebase.database.ServerValue.TIMESTAMP
  });
};

// ═══ LISTEN TO PLAYERS ═══
Network.listenPlayers = function(callback) {
  if (!Network.matchRef) return;
  
  const playersRef = Network.matchRef.child('players');
  Network.listeners.players = playersRef.on('value', snapshot => {
    const players = [];
    snapshot.forEach(child => {
      const p = child.val();
      p.userId = child.key;
      players.push(p);
    });
    callback(players);
  });
};

// ═══ BULLETS SYNC ═══
Network.spawnBullet = function(bullet) {
  if (!Network.matchRef) return;
  
  const bulletsRef = Network.matchRef.child('bullets');
  bulletsRef.push({
    x: Math.round(bullet.x),
    y: Math.round(bullet.y),
    vx: Math.round(bullet.vx),
    vy: Math.round(bullet.vy),
    damage: bullet.damage,
    owner: Network.userId,
    ownerName: Network.userName,
    createdAt: firebase.database.ServerValue.TIMESTAMP
  });
};

Network.listenBullets = function(callback) {
  if (!Network.matchRef) return;
  
  const bulletsRef = Network.matchRef.child('bullets');
  Network.listeners.bullets = bulletsRef.on('child_added', snapshot => {
    const b = snapshot.val();
    b.id = snapshot.key;
    callback(b);
  });
};

// ═══ CHAT ═══
Network.sendChat = async function(text) {
  if (!Network.matchRef) return;
  await Network.matchRef.child('chat').push({
    userId: Network.userId,
    userName: Network.userName,
    text: text,
    timestamp: firebase.database.ServerValue.TIMESTAMP
  });
};

Network.listenChat = function(callback) {
  if (!Network.matchRef) return;
  const chatRef = Network.matchRef.child('chat').limitToLast(20);
  Network.listeners.chat = chatRef.on('child_added', snapshot => {
    callback(snapshot.val());
  });
};

// ═══ CLEANUP ═══
Network.leaveMatch = async function() {
  if (!Network.matchRef) return;
  
  try {
    await Network.matchRef.child('players/' + Network.userId).remove();
    await Network.matchRef.child('playerCount').transaction(c => Math.max(0, (c || 1) - 1));
  } catch (e) {}
  
  // Remove listeners
  for (const key in Network.listeners) {
    // Clean up
  }
  
  Network.matchRef = null;
  Network.currentMatchId = null;
};

// ═══ DISCONNECT HANDLER ═══
Network.setupDisconnect = function() {
  if (!Network.matchRef) return;
  
  const playerRef = Network.matchRef.child('players/' + Network.userId);
  playerRef.onDisconnect().update({
    alive: false,
    disconnectedAt: firebase.database.ServerValue.TIMESTAMP
  });
};

// ═══ EXPORT ═══
window.Network = Network;
window.initNetwork = initNetwork;

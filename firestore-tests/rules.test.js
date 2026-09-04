const fs = require('fs');
const path = require('path');
const assert = require('assert');

const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require('@firebase/rules-unit-testing');
const {
  doc, getDoc, setDoc, updateDoc, deleteDoc, serverTimestamp, increment,
  collection, getDocs, query, where, orderBy, limit,
} = require('firebase/firestore');

const AUTHOR = 'author-uid';
const OTHER = 'other-uid';
const TEMPLATE_ID = 'template-1';

let testEnv;

const template = (overrides = {}) => ({
  id: TEMPLATE_ID,
  name: 'Test College - CSE Sem 3',
  college: 'Test College',
  department: 'CSE',
  semester: 3,
  section: 'A',
  academicYear: '2025-26',
  authorId: AUTHOR,
  authorName: 'Anonymous Student',
  likes: 0,
  downloads: 0,
  classes: [],
  subjects: [],
  createdAt: serverTimestamp(),
  updatedAt: serverTimestamp(),
  version: 1,
  ...overrides,
});

/** Writes bypassing rules, for arranging state. */
async function seed(fn) {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await fn(ctx.firestore());
  });
}

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'attendx-rules-test',
    firestore: {
      rules: fs.readFileSync(path.resolve(__dirname, '../firestore.rules'), 'utf8'),
      host: '127.0.0.1',
      port: 8080,
    },
  });
});

after(async () => { await testEnv.cleanup(); });
beforeEach(async () => { await testEnv.clearFirestore(); });

describe('templates', () => {
  it('requires auth even to read', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const anon = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(anon, 'templates', TEMPLATE_ID)));
  });

  it('lets any signed-in user read', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(OTHER).firestore();
    await assertSucceeds(getDoc(doc(db, 'templates', TEMPLATE_ID)));
  });

  it('allows a well-formed create by the author', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
  });

  it('rejects a create with a spoofed authorId', async () => {
    const db = testEnv.authenticatedContext(OTHER).firestore();
    await assertFails(setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
  });

  it('rejects a create that pre-seeds likes', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(
      setDoc(doc(db, 'templates', TEMPLATE_ID), template({ likes: 9999 })),
    );
  });

  it('rejects a create with a client-side createdAt', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(
      setDoc(doc(db, 'templates', TEMPLATE_ID), template({ createdAt: new Date() })),
    );
  });

  it('rejects an out-of-range semester', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(
      setDoc(doc(db, 'templates', TEMPLATE_ID), template({ semester: 99 })),
    );
  });

  it('lets the author update their own template', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      college: 'Renamed College',
      updatedAt: serverTimestamp(),
      version: increment(1),
    }));
  });

  it('stops a different user updating it', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(OTHER).firestore();
    await assertFails(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      college: 'Hijacked',
      updatedAt: serverTimestamp(),
      version: increment(1),
    }));
  });

  it('stops the author farming likes', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      likes: 9999,
      updatedAt: serverTimestamp(),
      version: increment(1),
    }));
  });

  it('requires the version to advance by exactly one', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      college: 'Renamed',
      updatedAt: serverTimestamp(),
      version: 50,
    }));
  });

  it('lets a legacy template with no version field still be edited', async () => {
    // Templates published before versioning existed must not become read-only.
    await seed(async (db) => {
      const legacy = template();
      delete legacy.version;
      delete legacy.updatedAt;
      await setDoc(doc(db, 'templates', TEMPLATE_ID), legacy);
    });
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      college: 'Renamed College',
      updatedAt: serverTimestamp(),
      version: increment(1),
    }));
  });

  it('lets anyone bump downloads by exactly one', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(OTHER).firestore();
    await assertSucceeds(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      downloads: increment(1),
    }));
  });

  it('stops downloads being inflated in one write', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(OTHER).firestore();
    await assertFails(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      downloads: increment(500),
    }));
  });

  it('only lets the author delete', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const other = testEnv.authenticatedContext(OTHER).firestore();
    await assertFails(deleteDoc(doc(other, 'templates', TEMPLATE_ID)));

    const author = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(deleteDoc(doc(author, 'templates', TEMPLATE_ID)));
  });
});

// Queries are evaluated differently from single-document gets: Firestore rejects a
// query it cannot prove every matched document would satisfy. These mirror the exact
// queries in TemplateRepositoryImpl and PeerSessionRepository.
describe('queries the app actually runs', () => {
  it('allows the marketplace query (orderBy likes desc, limit 50)', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(OTHER).firestore();
    await assertSucceeds(getDocs(query(
      collection(db, 'templates'), orderBy('likes', 'desc'), limit(50),
    )));
  });

  it('allows the filtered marketplace query (department + semester)', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(OTHER).firestore();
    await assertSucceeds(getDocs(query(
      collection(db, 'templates'),
      orderBy('likes', 'desc'),
      where('department', '==', 'CSE'),
      where('semester', '==', 3),
      limit(50),
    )));
  });

  it('allows getMyTemplates (whereEqualTo authorId)', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    const snap = await assertSucceeds(getDocs(query(
      collection(db, 'templates'), where('authorId', '==', AUTHOR),
    )));
    assert.strictEqual(snap.size, 1);
  });

  it('denies the marketplace query to an unauthenticated client', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDocs(query(
      collection(db, 'templates'), orderBy('likes', 'desc'), limit(50),
    )));
  });

  it('denies enumerating peer_sessions', async () => {
    // The app only ever reads these by id. Listing them would expose attendance
    // percentages in bulk, so the read rule must not be satisfiable as a query.
    await seed((db) => setDoc(doc(db, 'peer_sessions', 's1'), {
      sessionId: 's1', status: 'WAITING', hostAnonUid: 'host-uid',
      guestAnonUid: null, hostData: [], guestData: [], createdAt: serverTimestamp(),
    }));
    const db = testEnv.authenticatedContext('snooper').firestore();
    await assertFails(getDocs(collection(db, 'peer_sessions')));
  });
});

describe('usernames', () => {
  it('lets a user reserve a free name', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(setDoc(doc(db, 'usernames', 'jyotirmoy'), { uid: AUTHOR }));
  });

  it('rejects a second claim on a taken name', async () => {
    await seed((db) => setDoc(doc(db, 'usernames', 'jyotirmoy'), { uid: AUTHOR }));
    const db = testEnv.authenticatedContext(OTHER).firestore();
    // Create-only permission: the write fails because the document exists.
    await assertFails(setDoc(doc(db, 'usernames', 'jyotirmoy'), { uid: OTHER }));
  });

  it('rejects reserving a name for somebody else', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(setDoc(doc(db, 'usernames', 'someone'), { uid: OTHER }));
  });

  it('rejects an out-of-charset name', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(setDoc(doc(db, 'usernames', 'Has Space'), { uid: AUTHOR }));
    await assertFails(setDoc(doc(db, 'usernames', 'ab'), { uid: AUTHOR }));
  });

  it('only lets the holder release a name', async () => {
    await seed((db) => setDoc(doc(db, 'usernames', 'jyotirmoy'), { uid: AUTHOR }));
    const other = testEnv.authenticatedContext(OTHER).firestore();
    await assertFails(deleteDoc(doc(other, 'usernames', 'jyotirmoy')));

    const author = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(deleteDoc(doc(author, 'usernames', 'jyotirmoy')));
  });

  it('accepts an authorName backed by a reservation', async () => {
    await seed((db) => setDoc(doc(db, 'usernames', 'jyotirmoy'), { uid: AUTHOR }));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(
      setDoc(doc(db, 'templates', TEMPLATE_ID), template({ authorName: 'Jyotirmoy' })),
    );
  });

  it('rejects an authorName with no reservation', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(
      setDoc(doc(db, 'templates', TEMPLATE_ID), template({ authorName: 'Unclaimed' })),
    );
  });

  it('lets the owner keep editing after their username reservation is gone', async () => {
    // Renaming releases the old reservation without rewriting already-published
    // templates. If authorName were revalidated on every update, every one of
    // the author's older templates would silently become read-only.
    await seed((db) =>
      setDoc(doc(db, 'templates', TEMPLATE_ID), template({ authorName: 'Ghost' })));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      college: 'Renamed College',
      updatedAt: serverTimestamp(),
      version: increment(1),
    }));
  });

  it('still blocks changing authorName to an unreserved name', async () => {
    await seed((db) => setDoc(doc(db, 'templates', TEMPLATE_ID), template()));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      authorName: 'NotMine',
      updatedAt: serverTimestamp(),
      version: increment(1),
    }));
  });

  it('allows changing authorName to a name this uid reserved', async () => {
    await seed(async (db) => {
      await setDoc(doc(db, 'usernames', 'jyotirmoy'), { uid: AUTHOR });
      await setDoc(doc(db, 'templates', TEMPLATE_ID), template());
    });
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertSucceeds(updateDoc(doc(db, 'templates', TEMPLATE_ID), {
      authorName: 'Jyotirmoy',
      updatedAt: serverTimestamp(),
      version: increment(1),
    }));
  });

  it('rejects an authorName reserved by somebody else', async () => {
    await seed((db) => setDoc(doc(db, 'usernames', 'jyotirmoy'), { uid: OTHER }));
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(
      setDoc(doc(db, 'templates', TEMPLATE_ID), template({ authorName: 'Jyotirmoy' })),
    );
  });
});

describe('peer_sessions', () => {
  const SESSION = 'session-1';
  const HOST = 'host-uid';
  const GUEST = 'guest-uid';

  const session = (overrides = {}) => ({
    sessionId: SESSION,
    status: 'WAITING',
    hostAnonUid: HOST,
    guestAnonUid: null,
    hostData: [{ normalizedName: 'maths', originalName: 'Maths', attendancePercentage: 80 }],
    guestData: [],
    createdAt: serverTimestamp(),
    ...overrides,
  });

  it('lets the host create a waiting session', async () => {
    const db = testEnv.authenticatedContext(HOST).firestore();
    await assertSucceeds(setDoc(doc(db, 'peer_sessions', SESSION), session()));
  });

  it('rejects creating a session on somebody else behalf', async () => {
    const db = testEnv.authenticatedContext(GUEST).firestore();
    await assertFails(setDoc(doc(db, 'peer_sessions', SESSION), session()));
  });

  it('lets a guest read a waiting session and join it', async () => {
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session()));
    const db = testEnv.authenticatedContext(GUEST).firestore();

    await assertSucceeds(getDoc(doc(db, 'peer_sessions', SESSION)));
    await assertSucceeds(updateDoc(doc(db, 'peer_sessions', SESSION), {
      guestAnonUid: GUEST,
      guestData: [],
      status: 'PAIRED',
    }));
  });

  it('hides a paired session from a third party', async () => {
    // Attendance data must not stay readable to anyone who later learns the id.
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session({
      status: 'PAIRED', guestAnonUid: GUEST,
    })));
    const stranger = testEnv.authenticatedContext('stranger-uid').firestore();
    await assertFails(getDoc(doc(stranger, 'peer_sessions', SESSION)));
  });

  it('still lets both participants read a paired session', async () => {
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session({
      status: 'PAIRED', guestAnonUid: GUEST,
    })));
    for (const uid of [HOST, GUEST]) {
      const db = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(getDoc(doc(db, 'peer_sessions', SESSION)));
    }
  });

  it('stops a third party hijacking an already-paired session', async () => {
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session({
      status: 'PAIRED', guestAnonUid: GUEST,
    })));
    const db = testEnv.authenticatedContext('stranger-uid').firestore();
    await assertFails(updateDoc(doc(db, 'peer_sessions', SESSION), {
      guestAnonUid: 'stranger-uid',
      guestData: [],
      status: 'PAIRED',
    }));
  });

  it('stops a guest rewriting the host attendance data', async () => {
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session()));
    const db = testEnv.authenticatedContext(GUEST).firestore();
    await assertFails(updateDoc(doc(db, 'peer_sessions', SESSION), {
      guestAnonUid: GUEST,
      guestData: [],
      status: 'PAIRED',
      hostData: [],
    }));
  });

  it('lets either participant erase the session', async () => {
    // The app deletes rather than flagging EXPIRED, because the peer screen
    // promises the data is erased when the session ends.
    for (const uid of [HOST, GUEST]) {
      await testEnv.clearFirestore();
      await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session({
        status: 'PAIRED', guestAnonUid: GUEST,
      })));
      const db = testEnv.authenticatedContext(uid).firestore();
      await assertSucceeds(deleteDoc(doc(db, 'peer_sessions', SESSION)));
    }
  });

  it('lets the host erase a WAITING session that nobody joined', async () => {
    // This is the exact path "Cancel Session" takes, and the one the 15-minute
    // timer takes: guestAnonUid is still null, so participant() must hold on the
    // host clause alone.
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session()));
    const db = testEnv.authenticatedContext(HOST).firestore();
    await assertSucceeds(deleteDoc(doc(db, 'peer_sessions', SESSION)));
  });

  it('stops a third party erasing someone else session', async () => {
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session({
      status: 'PAIRED', guestAnonUid: GUEST,
    })));
    const db = testEnv.authenticatedContext('stranger-uid').firestore();
    await assertFails(deleteDoc(doc(db, 'peer_sessions', SESSION)));
  });

  it('stops a non-participant erasing a waiting session', async () => {
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session()));
    const db = testEnv.authenticatedContext('stranger-uid').firestore();
    await assertFails(deleteDoc(doc(db, 'peer_sessions', SESSION)));
  });

  it('lets a participant expire the session', async () => {
    await seed((db) => setDoc(doc(db, 'peer_sessions', SESSION), session({
      status: 'PAIRED', guestAnonUid: GUEST,
    })));
    const db = testEnv.authenticatedContext(HOST).firestore();
    await assertSucceeds(updateDoc(doc(db, 'peer_sessions', SESSION), { status: 'EXPIRED' }));
  });
});

describe('collections with no rules', () => {
  it('denies reads and writes by default', async () => {
    const db = testEnv.authenticatedContext(AUTHOR).firestore();
    await assertFails(setDoc(doc(db, 'anything_else', 'x'), { a: 1 }));
    await assertFails(getDoc(doc(db, 'anything_else', 'x')));
  });
});

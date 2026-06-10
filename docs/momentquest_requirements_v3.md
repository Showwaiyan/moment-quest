# MomentQuest — App Requirements Document

**Version:** 3.0  
**Date:** June 2026  
**Course:** COS30017 — Software Development for Mobile Devices (Assignment 3)  
**Platform:** Android (Kotlin), minimum API 26 (Android 8.0 Oreo)  
**Backend:** Firebase (Firestore + Firebase Storage)

---

## 1. Vision

MomentQuest is a personal life-capture Android app that empowers users to set meaningful personal challenges — inspired by New Year's resolutions and bucket lists — and spontaneously record unexpected meaningful moments that happen along the way.

Unlike pure task managers (Todoist, TickTick) that treat life as a productivity to-do list, or pure journals (Day One, Reflectly) that are passive diaries, MomentQuest occupies a unique middle ground: it is **goal-oriented yet emotionally open**. A user sets a challenge ("Go to the beach this year"), completes it, and captures the memory with a photo, GPS location, and personal notes. But they can also tap a button right now to record an unplanned encounter ("Just ran into my old friend in Subang") with the same richness. The result is a **living timeline of the user's year** — both the things they planned and the things life surprised them with.

### Target Audience

- Young adults (18–30) who set personal yearly goals or resolutions
- Students who want a lightweight life journal without the complexity of a full diary app
- Anyone who values memories over productivity metrics

### Differentiator vs. Existing Apps

| App | Type | Limitation vs. MomentQuest |
|---|---|---|
| Todoist / TickTick | Task manager | No memory capture, no emotional context |
| Day One / Reflectly | Journal | No structured goal-setting |
| Wanderlist / Futurenda | Bucket list | Minimal completion detail; no spontaneous capture |
| **MomentQuest** | **Life-capture hybrid** | Combines planned challenges + spontaneous moments |

---

## 2. Core Concepts

### 2.1 Challenge
A pre-planned goal a user intends to accomplish within a timeframe. Has a lifecycle: **Pending → Completed**. When completed, a Memory entry is attached.

### 2.2 Moment
A spontaneous, unplanned journal entry captured on the spot. Always tied to current date/time and GPS location. It is not a goal — it is a memory in itself.

### 2.3 Memory
A rich completion record attached to a Challenge when marked done. Contains photo, GPS coordinates, completion date, and notes. A Challenge can have one or more Memory entries.

---

## 3. User Stories

### Challenge Management

| ID | As a... | I want to... | So that... |
|---|---|---|---|
| US-01 | User | Create a new challenge with title, category, and optional deadline | I can plan my goals for the year |
| US-02 | User | View all challenges filtered by status | I can see what I still need to accomplish |
| US-03 | User | Edit an existing challenge | I can update goals if circumstances change |
| US-04 | User | Delete a challenge | I can keep my list focused |
| US-05 | User | Mark a challenge complete and attach a Memory | I can record how and where I accomplished it |
| US-06 | User | Filter challenges by category or status | I can find specific goals quickly |

### Moment Capture

| ID | As a... | I want to... | So that... |
|---|---|---|---|
| US-07 | User | Quickly capture a spontaneous Moment | I don't lose unexpected memories |
| US-08 | User | Attach a photo to a Moment | I can visually remember the experience |
| US-09 | User | Have GPS auto-recorded on a Moment | I can remember exactly where I was |
| US-10 | User | Edit or delete a Moment | I can correct mistakes or remove private entries |
| US-11 | User | Add a mood tag (Happy, Grateful, Surprised, Reflective) | I can emotionally categorise my memories |

### Timeline & Discovery

| ID | As a... | I want to... | So that... |
|---|---|---|---|
| US-12 | User | See a unified timeline of Challenges and Moments | I have one scrollable view of my year |
| US-13 | User | See a summary stats screen | I can feel a sense of progress |
| US-14 | User | Search by keyword | I can find a memory without scrolling everything |

---

## 4. Use Cases

### UC-01: Create a Challenge
- **Actor:** User
- **Precondition:** App open on Home Timeline
- **Trigger:** FAB → "Set a Challenge"
- **Main Flow:**
  1. App navigates to Add Challenge Fragment
  2. User enters title, selects category, optionally sets deadline
  3. User taps Save; ViewModel validates (title required)
  4. Repository writes to Firestore via Coroutine
  5. LiveData triggers RecyclerView refresh
- **Alternate Flow:** Validation fails → inline error; no Firestore write

### UC-02: Complete a Challenge and Add a Memory
- **Actor:** User
- **Precondition:** Challenge with status PENDING exists
- **Trigger:** Challenge Detail → "Mark as Complete"
- **Main Flow:**
  1. Add Memory bottom sheet opens
  2. User adds notes, optional photo (Camera Intent or Gallery)
  3. App fetches GPS via FusedLocationProviderClient
  4. Photo uploaded to Firebase Storage; URL saved
  5. Memory document created in Firestore linked by `challengeId`
  6. Challenge `status` updated to COMPLETED
- **Exception:** GPS unavailable → null stored; Snackbar shown

### UC-03: Capture a Spontaneous Moment
- **Actor:** User
- **Precondition:** App open (any screen)
- **Trigger:** FAB → "Capture a Moment"
- **Main Flow:**
  1. Add Moment Fragment opens
  2. User enters title, optional description, mood chip
  3. Optional photo attached; GPS auto-fetched
  4. `createdAt` set to current timestamp
  5. Moment document written to Firestore via Coroutine
  6. Timeline refreshes
- **Exception:** Camera permission denied → photo skipped; Snackbar shown

### UC-04: View Unified Timeline
- **Actor:** User
- **Trigger:** App launch or Home navigation
- **Main Flow:**
  1. ViewModel queries Firestore for both Challenges and Moments, ordered by `createdAt` desc
  2. Repository merges into single sorted feed
  3. RecyclerView renders two view types: Challenge Card and Moment Card
  4. Tap navigates to Detail screen

### UC-05: Delete a Challenge or Moment
- **Actor:** User
- **Trigger:** Long-press or Delete button in Detail screen
- **Main Flow:**
  1. Confirmation dialog shown
  2. Firestore document deleted via Coroutine; Memory sub-documents batch-deleted
  3. Firebase Storage photo file deleted if present
  4. RecyclerView updates via LiveData with animation

---

## 5. Functional Requirements

| ID | Requirement |
|---|---|
| FR-01 | Full CRUD on Challenges in Firestore |
| FR-02 | Full CRUD on Moments in Firestore |
| FR-03 | Create and Read on Memory entries linked to Challenges |
| FR-04 | Unified timeline RecyclerView with two distinct view types |
| FR-05 | GPS capture via FusedLocationProviderClient on Memory and Moment creation |
| FR-06 | Photo capture via Camera Intent or Gallery selection |
| FR-07 | Photo upload to Firebase Storage; download URL stored in Firestore |
| FR-08 | ViewModel and LiveData for all UI state |
| FR-09 | All Firestore/Storage operations via Kotlin Coroutines on IO dispatcher |
| FR-10 | Runtime permission handling for Camera and Location |
| FR-11 | Stats Fragment: total challenges, completed count, completion rate, total moments |
| FR-12 | Challenge filtering by status (All/Pending/Completed) and category |

---

## 6. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-01 | **Performance:** Timeline loads within 2 seconds on stable connection |
| NFR-02 | **Error Handling:** Snackbar shown if Firestore unreachable; no crash |
| NFR-03 | **Responsiveness:** Usable on 360dp–420dp screen widths |
| NFR-04 | **Security:** API keys only in `google-services.json`, excluded via `.gitignore` |
| NFR-05 | **Accessibility:** 48×48dp touch targets; `contentDescription` on all ImageViews |
| NFR-06 | **Code Style:** Android Kotlin style guide (4-space indent, meaningful naming) |
| NFR-07 | **Architecture:** Strict MVVM + Repository pattern throughout |
| NFR-08 | **Version Control:** Git with descriptive commit messages |

---

## 7. Technical Architecture

### 7.1 MVVM + Repository
UI Layer (Activities / Fragments + ViewBinding)
↕ observe LiveData / call ViewModel
ViewModel Layer (ChallengeViewModel, MomentViewModel)
↕ suspend functions
Repository Layer (ChallengeRepository, MomentRepository)
↕ SDK calls
Data Layer (Firestore + Firebase Storage)

### 7.2 Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | XML Layouts + View Binding |
| Architecture | MVVM + Repository |
| Database | Firebase Firestore |
| File Storage | Firebase Storage |
| Async | Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`) |
| State | `MutableLiveData` / `LiveData` |
| Location | `FusedLocationProviderClient` |
| Image Loading | Glide |
| Navigation | Intents + Fragment Manager |

### 7.3 Firestore Data Model

**Collection: `challenges`**
```json
{
  "id": "auto",
  "title": "Go to the beach",
  "category": "Travel",
  "deadline": null,
  "status": "PENDING",
  "createdAt": "Timestamp"
}
```

**Collection: `moments`**
```json
{
  "id": "auto",
  "title": "Met old friend",
  "description": "...",
  "mood": "Happy",
  "photoUrl": "https://...",
  "latitude": 3.1390,
  "longitude": 101.6869,
  "createdAt": "Timestamp"
}
```

**Sub-collection: `challenges/{challengeId}/memories`**
```json
{
  "id": "auto",
  "notes": "...",
  "photoUrl": "https://...",
  "latitude": 2.7297,
  "longitude": 101.9381,
  "completedAt": "Timestamp"
}
```

### 7.4 Screens / Fragments Map

| Screen | Type | Key Components |
|---|---|---|
| Home Timeline | Fragment + RecyclerView | Multi-viewtype adapter, FAB |
| Add / Edit Challenge | Fragment | EditText, Spinner, DatePickerDialog |
| Challenge Detail | Activity | Info + Memories list, "Mark Complete" button |
| Add Memory | Fragment (Bottom Sheet) | Camera Intent, GPS, EditText |
| Add / Edit Moment | Fragment | Camera Intent, GPS, ChipGroup |
| Stats / Summary | Fragment | TextViews, ProgressBar |

---

## 8. UI/UX Prototype Plan

### 8.1 Low-Fidelity Wireframes
Hand-drawn or Figma wireframes for all 6 screens to be produced before development and included in the report.

### 8.2 High-Fidelity Prototype
Clickable Figma prototype connecting all 6 screens, produced before implementation.

### 8.3 User Testing Plan
Minimum **3 participants** testing these tasks:
1. Add a challenge "Read a book" in the Learning category
2. Capture a spontaneous Moment with a photo
3. Mark a challenge complete and attach a memory
4. Find the Stats screen and report total moments

Feedback collected via a **5-question SUS survey**. Results and UI changes documented in the report.

---

## 9. Investigation Plan (Part 2 — 10 marks)

**Research Question:** Does a bottom sheet modal for adding entries result in faster task completion compared to a full-screen Activity?

**Hypothesis:** Users complete "Add Moment" faster with a bottom sheet (fewer navigation transitions, visual context maintained).

**Method:**
1. Implement both variants in a test branch
2. Recruit 4 participants; measure time-on-task and ease rating (1–5) for both designs
3. Compare results and justify final design choice in the 3-minute investigation video

**Expected Outcome:** Bottom sheet scores lower time-on-task and higher ease rating.

---

## 10. Development Timeline

| Week | Planned Tasks | Est. Hours |
|---|---|---|
| Week 9 | Requirements, wireframes, Figma prototype | 5h |
| Week 10 | Firebase setup, Firestore schema, MVVM scaffold | 6h |
| Week 11 | Home Timeline (RecyclerView, multi-viewtype adapter) | 5h |
| Week 12 | Add/Edit Challenge + Challenge Detail Activity | 5h |
| Week 13 | Add Moment Fragment (Camera, GPS, Firestore) | 5h |
| Week 14 | Add Memory, Stats Fragment, filters, bug fixes | 5h |
| Week 15 | User testing, Investigation experiment, report writing | 6h |
| **Total** | | **~37h** |

---

## 11. Limitations

| Limitation | Reason |
|---|---|
| No user authentication | Single anon Firebase project; no login in v1 |
| No offline cache | Firestore offline persistence not configured in v1 |
| No photo compression | Large photos may be slow to upload |
| No map view for GPS | Coordinates stored but not rendered on a map |
| No push notifications | WorkManager/AlarmManager deferred to v2 |
| No dark mode | Single light theme to focus on core functionality |
| Jetpack Compose not used | XML layouts required per COS30017 spec |

---

## ⚠️ Important Assignment Warnings

### Academic Integrity
- **Do NOT share code or report** with other students — giving work to another student carries the same penalty as copying (failure + official notice + possible exclusion).
- **Do NOT pay anyone** to complete this assignment — highest form of academic misconduct, may result in degree revocation.
- **≥25% Turnitin similarity** triggers review; copied sections awarded **0** for that criterion.

### GenAI Usage
- **All GenAI use must be acknowledged** — include prompts and outputs in an appendix of the report.
- If GenAI was NOT used, write: *"No Generative AI tools were used for this task."*
- Submitting AI output as your own without acknowledgement = redo or penalty.

### Demo Requirement
- **Live demo in Week 13 or 14 is mandatory** — failure to demo = **0 for the entire assignment**.
- Aim for **Week 13** for higher marks.
- Additional interview may be required in Weeks 14–15.

### Submission
- Submit a **single PDF** report to Canvas.
- Submit **Android Studio project as a separate ZIP** (no report inside).
- Investigation video submitted to a **separate submission point** — not inside the main report ZIP.
- Videos on **Canvas or private YouTube only** — not Google Drive.

### Disallowed Technologies
- **Jetpack Compose NOT permitted** for Part 1.
- Data storage must be **SQLite, Room, Firebase, or file-based** — services like Amplify not permitted.

### Late Penalty
- **10% per late day**, max 5 working days. No extensions beyond 5 days without special consideration.

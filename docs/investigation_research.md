# MomentQuest — Investigation Plan (Part 2)

**Course:** COS30017 — Software Development for Mobile Devices  
**Assignment:** Assignment 3 — Investigation Video (Part 2)  
**Due:** Wednesday, 23:59 (available until 19 Jun 23:59)  
**Marks:** 10 points  
**Format:** 3-minute video — NOT the same as the Part 1 demo

---

## ⚠️ Key Rules from the Spec

- Separate 3-minute video submitted to a different submission point from Part 1
- Must compare two variations in a proper experiment — animations alone not sufficient
- Jetpack Compose may be used for this part only (exception to the Compose restriction)
- Must follow: Aim → Method → Results → Discussion/Recommendations
- Suitable APA 7 references must be included
- Show consistent progress to tutor throughout the assessment period
- The investigation must enhance the final app — not be an isolated experiment

---

## Rubric Summary

| Level | Score | What's Required |
|---|---|---|
| **Excellent** | 7–10 | Clear aim/method/results/discussion, relevant topic, suitable depth, consistent progress, references, enhances app |
| **Good** | 3–7 | Some story, relevant topic, some depth, some progress, some references |
| **Needs Improvement** | 0–3 | Minimal story, not relevant, no depth, no progress, no references |
| **No Attempt** | 0 | Nothing submitted |

---

## Chosen Investigation Topic

### Usability Study: Bottom Sheet Modal vs. Full-Screen Activity for Entry Creation

**Why this topic?**
- Directly relevant to UI design and mobile patterns (ULOs)
- Tests a real design decision in MomentQuest (how users create Challenges and Moments)
- Result directly informs and improves the final app
- Aligns with suggested topic: "Undertaking a usability study comparing two interfaces, making use of mobile-focused patterns and usage"

---

## Investigation Structure

### Aim
To determine whether a **bottom sheet modal** or a **full-screen Activity** results in better usability (faster task completion and higher satisfaction) when capturing a new Moment in MomentQuest.

### Background / Rationale
Bottom sheets are a Material Design pattern that keeps users in context while performing a secondary action. Full-screen Activities interrupt the user's flow by replacing the current screen. For a quick "Capture a Moment" action — time-sensitive by nature — reducing screen transitions may meaningfully improve UX.

**References to include:**
- Google Material Design 3 — Bottom Sheet guidelines
- Nielsen Norman Group — Mobile UX patterns
- Academic paper on mobile task completion time (ACM Digital Library)

### Hypothesis
Users will complete the "Capture a Moment" task **faster** and rate it **easier** when using a bottom sheet compared to a full-screen Activity.

---

## Method

### Variants

**Variant A — Full-Screen Activity**
- FAB → new Activity opens (full screen transition)
- User fills form, taps Save, returns to timeline

**Variant B — Bottom Sheet Modal**
- FAB → bottom sheet slides up over the timeline
- User fills form, taps Save, sheet dismisses, timeline updates in place

Both variants use identical form fields: title, description, mood chip, photo button.

### Participants
- Recruit 4–5 participants (classmates, friends)
- Each tests **both variants** in counterbalanced order to control for learning effects

### Measurements
1. **Time-on-task (seconds):** From FAB tap to Save tap, measured with stopwatch
2. **Ease rating (1–5 Likert):** "How easy was it to capture a new Moment? (1=Very Difficult, 5=Very Easy)"
3. **Error count:** Number of wrong taps or re-attempts

### Procedure
1. Brief participant: "Imagine you just ran into an old friend. Open the app and capture this as a Moment."
2. Start stopwatch when FAB is tapped
3. Stop when Save is tapped
4. Ask ease rating
5. 2-minute break, then repeat with other variant
6. Record all results

### Results Table

| Participant | Variant A Time (s) | Variant A Ease | Variant B Time (s) | Variant B Ease |
|---|---|---|---|---|
| P1 | | | | |
| P2 | | | | |
| P3 | | | | |
| P4 | | | | |
| **Avg** | | | | |

---

## Discussion Points

- Which variant had lower average time-on-task?
- Which had higher average ease rating?
- Did any participants make errors in one variant but not the other?
- Are results consistent with Material Design recommending bottom sheets for quick secondary actions?
- **Recommendation:** Which variant is adopted in MomentQuest and why?

---

## How This Enhances the Final App

The winning variant will be implemented as the default entry creation mechanism in the final submitted MomentQuest app. This demonstrates the UI decision was evidence-based — exactly what the Excellent rubric level requires.

---

## Video Script Outline (3 minutes)

| Time | Content |
|---|---|
| 0:00–0:20 | Intro to MomentQuest — what it does (don't assume marker knows) |
| 0:20–0:50 | State research question and why it matters for MomentQuest |
| 0:50–1:20 | Show Variant A and Variant B side-by-side in screen recording |
| 1:20–2:00 | Present results table — time-on-task and ease ratings |
| 2:00–2:40 | Discuss results — which won and why, link to Material Design |
| 2:40–3:00 | Recommendation: what was changed in the final app as a result |

> **Note:** Video must be self-contained. Briefly explain MomentQuest at the start — the marker may not have seen your Part 1 app.

---

## Submission Checklist

- [ ] 3-minute video recorded (screen recording + voiceover recommended)
- [ ] Uploaded to Canvas or private YouTube (not Google Drive)
- [ ] Submitted to Investigation Video submission point (Part 2) — NOT inside main report ZIP
- [ ] APA 7 references included (slide at end of video or in description)
- [ ] Progress shown to tutor during Weeks 13–15

---

## References (APA 7 — complete before submission)

Google. (2023). *Bottom sheets — Material Design 3*. https://m3.material.io/components/bottom-sheets/overview

Nielsen Norman Group. (2022). *Mobile UX design patterns*. https://www.nngroup.com/topic/mobile-usability/

*(Add 1–2 academic sources from ACM Digital Library or IEEE Xplore on mobile usability testing)*

# MomentQuest: Slide Deck - App Introduction & Vision

This markdown file represents the introductory slides for your presentation. Each slide is separated by a horizontal rule (`---`) and includes **Slide Content** and **Speaker Notes** to guide you during the talk.

---

# Slide 1: Title
## **MomentQuest**
### *Capturing the Plans and Surprises of Life*

**Presented by:** [Your Name]  
**Course:** COS30017 — Software Development for Mobile Devices

---

* **Speaker Notes:**
  > Good morning/afternoon everyone. Today, I am excited to introduce you to **MomentQuest**, an Android application that reimagines how we capture our personal journeys. Most apps ask us to track our tasks or log our diaries, but MomentQuest does something different: it captures both the structured goals we set for ourselves and the spontaneous, unplanned moments that make life beautiful.

---

# Slide 2: The Core Problem
## **Productivity Fatigue vs. Journal Apathy**

*   **Task Managers (Todoist, TickTick):**
    *   Treat life like a sterile "to-do list" check-box.
    *   Focus on output, ignoring emotional context and memories.
*   **Traditional Journals (Day One, Reflectly):**
    *   Require high friction (writing long diary entries daily).
    *   Offer passive logs with no active goal-setting or drive.
*   **The Gap:** Where do we record the journey *between* setting a goal and completing it?

---

* **Speaker Notes:**
  > Let's start with a problem. We are surrounded by apps that help us track our lives, but they fall into two extremes. On one hand, task managers make our lives feel like a business spreadsheet—once a task is checked off, it disappears, leaving no emotional record. On the other hand, writing in a digital diary requires a lot of daily discipline, and they lack structured goals. MomentQuest was born out of a desire to bridge this gap.

---

# Slide 3: The Vision
## **A Living Timeline of Your Journey**

*   **The Concept:** Goal-oriented yet emotionally open.
*   **Hybrid Experience:**
    *   **The Planned:** Clear, categorized personal challenges (bucket lists, yearly resolutions).
    *   **The Spontaneous:** Instant, rich capture of unexpected events as they occur.
*   **The Result:** A timeline of what you set out to do, what actually happened, and how you felt along the way.

---

* **Speaker Notes:**
  > The vision of MomentQuest is to create a living timeline of your year. It represents a hybrid experience. It helps you set structured personal challenges—like "Go to the beach" or "Learn Kotlin"—but it also makes it incredibly easy to spontaneously record unplanned highlights, like running into an old friend at a cafe. The result is a timeline that tells the full story of your life, capturing both your intentions and life's surprises.

---

# Slide 4: Core Pillars (1/3)
## **1. The Challenge (The Planned)**

*   **What it is:** A pre-planned goal or milestone a user wants to achieve.
*   **Metadata:** Title, Category (Travel, Fitness, Social, Learning, Career, etc.), and optional Deadlines.
*   **Lifecycle:**
    $$\text{Pending} \longrightarrow \text{Completed}$$
*   **Purpose:** Keeps you motivated, structured, and focused on growth.

---

* **Speaker Notes:**
  > MomentQuest is built on three core pillars. The first is **The Challenge**. These are your goals—things you actively plan to do. You can categorize them, set deadlines, and track them from "Pending" to "Completed." It acts as a bucket list built directly into your pocket.

---

# Slide 5: Core Pillars (2/3)
## **2. The Moment (The Spontaneous)**

*   **What it is:** A quick, unplanned journal entry captured on the fly.
*   **Context Richness:**
    *   **Visuals:** Immediate camera or gallery photo attachment.
    *   **Location:** Automated background GPS capture (Latitude & Longitude).
    *   **Emotion:** Interactive mood tag chips (Happy, Grateful, Surprised, Reflective).
*   **Purpose:** Zero-friction capturing of sudden memories before they fade.

---

* **Speaker Notes:**
  > The second pillar is **The Moment**. Unlike a challenge, a Moment is spontaneous. When something unexpected happens, you tap a button, type a quick description, snap a photo, pick a mood tag, and the app automatically captures your GPS coordinates. It takes seconds, removing the friction of traditional journaling.

---

# Slide 6: Core Pillars (3/3)
## **3. The Memory (The Connection)**

*   **What it is:** The concrete record created when a Challenge is marked complete.
*   **The Connection:** Bridges your planned goals with your real-world experiences.
*   **Rich Details:** Includes completion notes, photos, and geographical coordinates.
*   **Purpose:** Archiving the story of how you accomplished your goals.

---

* **Speaker Notes:**
  > The final pillar is **The Memory**. When you complete a challenge, you don't just check a box. The app prompts you to create a "Memory." You upload a photo of your victory, write a reflection, and save the coordinates of where it happened. This turns your finished goals into lasting stories.

---

# Slide 7: Why MomentQuest is Different
## **Product Differentiation Matrix**

| Metric | Todoist / Task Apps | Day One / Journals | **MomentQuest** |
| :--- | :--- | :--- | :--- |
| **Structured Goals** | Yes (Tasks) | No | **Yes (Challenges)** |
| **Spontaneous Logging** | No | Yes (Text focus) | **Yes (Moments)** |
| **Automatic Context** | No | No / Manual | **Yes (Auto-GPS + Mood)** |
| **Data Preservation** | Minimal | High | **High (Photos + Memories)** |
| **Emotional Context** | None | Low | **High (Mood-mapping)** |

---

* **Speaker Notes:**
  > To show how MomentQuest is unique, let's look at this comparison. Task managers have goals but lack media and emotion. Journaling apps support media but lack goals. MomentQuest brings both together. By combining pre-planned challenges and spontaneous moment-logging under a single chronological timeline, users get a complete record of both their achievements and their unexpected memories.

---

# Slide 8: Technical Innovation (Bonus Slide)
## **Robust Architecture & UX Optimization**

*   **Kotlin Coroutines:** Asynchronous database and network processing on `Dispatchers.IO` to maintain 60 FPS UI performance.
*   **Reverse Geocoding Engine:** Local cache resolves latitude/longitude into real street addresses and cities to maximize readability.
*   **A/B Usability Testing:** Built-in usability tracking engine logs user completion speeds and ease ratings for different entry forms (Activity vs. Bottom Sheet) to dynamically optimize layouts.

---

* **Speaker Notes:**
  > Before I jump into the live demo, I want to briefly mention the technical engineering behind the app. It's built using Kotlin and follows clean architectural patterns. We use Coroutines to keep the app lag-free, a custom reverse geocoding engine to turn raw coordinates into street names, and a built-in usability tracking system that runs A/B testing directly inside the app to compare layouts. Now, let's transition to the device for a live demonstration of MomentQuest in action!

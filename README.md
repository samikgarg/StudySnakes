# 📚 StudySnakes 🎲

## 📌 Description
StudySnakes is an **Android application** designed to **gamify the studying experience** by integrating the classic **Snakes and Ladders** board game with subject-based quizzes. Players answer questions to advance in the game, reinforcing learning through interactive gameplay. The app includes **AI-powered question generation, database management for custom questions, and detailed performance analysis** to help users track and improve their learning outcomes.

---

## 🚀 Features

### 🎮 Gamified Learning
- Players progress through **Snakes and Ladders** by correctly answering subject-specific questions.
- Questions are randomly selected based on **accuracy rates**, prioritizing questions the user struggles with.
- **Adaptive Dice Rolls:** Correct answers increase the likelihood of favorable dice rolls (80-20 probability).

### 🏗️ Customizable Question Database
- **Add/Edit/Delete Questions**: Users can **manage their own question sets**.
- **Dynamic Subject & Topic Management**: New **subjects and topics** can be added, automatically updating the database.
- **Error Handling System**: Detects **missing fields, incorrect data entries, and invalid associations between subjects and topics**.

### 🧠 AI-Powered Question Generation
- **Leverages OpenAI API** to generate **relevant questions** based on a user-provided topic description.
- Users can **modify AI-generated questions** before saving them.

### 🎲 Gameplay Mechanics
- **Play Against a Bot or Another Player**.
- **Dice Animation:** The dice cycles through all faces before landing on a result.
- **Snakes and Ladders Logic:** Uses the **Pythagorean Theorem** to **calculate movement animation timing**, ensuring a smooth player movement experience.
- **Win Conditions:** The first player to **reach the final square wins**.

### 🔍 Advanced Question Filtering
- **SQL-Based Filtering System**: Users can filter questions for the game using:
  - **Subject & Topic**
  - **Question Content**
  - **Times Asked**
  - **AND/OR logic** to combine multiple filters
- **Error Handling**: Detects **invalid or empty filters**.

### 📊 Performance Tracking & Analysis
- Tracks **accuracy rates for specific subjects, topics, or question types**.
- Users can **set accuracy goals** and receive insights on:
  - **How many more correct answers** are needed to reach the goal.
  - **How many wrong answers** can be given while staying above the goal.
  - **If the goal is unachievable**.
- **Graphical Analysis** of question performance based on selected filters.

---

## 🏗️ Technologies Used

- **Java (Android SDK)** – Core development.
- **SQLite** – Storing and managing user-defined questions and performance data.
- **OpenAI API** – AI-powered question generation.
- **SQL Query Execution** – Advanced filtering and retrieval.
- **Android Animation API** – Smooth dice rolling and player movements.
- **Custom Error Handling System** – Prevents invalid database entries.
- **Pythagorean Theorem-Based Movement Calculation** – Ensuring consistent animation speed.

---

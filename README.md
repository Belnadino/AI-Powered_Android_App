# HealthCare Plus

An AI-powered Android application designed to support older adults in managing daily health routines, medication schedules, exercise plans, and health-related queries through an accessible and user-centred interface.

Developed as part of:

**COMP53615 – Human-AI Interaction Frameworks & Practices**  
**Department of Computer Science**  
**Durham University**

## Project Overview

HealthCare Plus addresses challenges faced by older adults, including:

- Memory-related difficulties
- Medication adherence
- Routine management
- Access to health guidance
- Communication with caregivers

The application combines Human-Centred Design principles with Artificial Intelligence to create an accessible healthcare assistant that promotes independence while maintaining trust, transparency, and user autonomy.

---

## Key Features

### Routine Management
- Create medication schedules
- Meal planning reminders
- Exercise scheduling
- Daily activity tracking

### AI Health Assistant
- OpenAI-powered conversational chatbot
- General health guidance
- Routine and wellbeing support
- Safe-response guardrails
- Redirects users to healthcare professionals when appropriate

### Notifications and Reminders
- Automated reminders
- Missed activity detection
- Configurable notification settings
- Sound and vibration controls

### Activity Logs and Reporting
- View completed activities
- Monitor missed routines
- Generate caregiver reports
- Consent-based data sharing

### Accessibility Features
- Adjustable font sizes
- High-contrast interface
- Large touch targets
- Voice input support
- Simplified navigation

### User Management
- User registration
- PIN login
- Guest access mode
- Profile management

---

## Video
Look Video to see how screen have been organised

---

## System Architecture

```text
User
    │
    ▼
Android Application (Kotlin)
    │
    ├── Room Database (Local Storage)
    │
    ├── Notification Manager
    │
    ├── Firebase Authentication
    │
    └── OpenAI API
             │
             ▼
      AI Chatbot Assistant
```

---

## Technology Stack

### Mobile Development

* Kotlin
* Android Studio
* Jetpack Compose
* XML Layouts

### Artificial Intelligence

* OpenAI API
* Large Language Models (LLMs)
* Prompt Engineering

### Database

* Room Database (SQLite)

### Authentication

* Firebase Authentication

### Notifications

* Android Notification Manager
* AlarmManager / WorkManager

---

## Research Foundations

The application was designed using:

* Human-Centred Design (ISO 9241-210)
* Inclusive Design Principles
* Responsible AI Principles
* Trustworthy AI Frameworks
* Accessibility Guidelines (WCAG)

Special attention was given to:

* Older adult usability
* Cognitive accessibility
* Technology trust
* Human-AI collaboration

---

## Installation

### Clone Repository

```bash
git clone git@github.com:Belnadino/AI-Powered_Android_App.git
cd AI-Powered_Android_App
```

### Open Project

Open the project in Android Studio.

### Configure OpenAI API Key

Create a local configuration file:

```properties
OPENAI_API_KEY=YOUR_API_KEY
```


### Build and Run

```bash
Build → Make Project
Run → Run App
```

---

## Current Limitations

* Chatbot provides general guidance only
* Not intended for medical diagnosis
* Email reporting uses prototype implementation
* Real-world clinical evaluation has not yet been conducted
* Long-term user engagement has not yet been evaluated

---

## Future Work

* Secure cloud backend
* Full email verification
* OTP verification
* Enhanced caregiver portal
* Fine-tuned healthcare language models
* Emotion-aware conversational AI
* Real-world usability studies with older adults

---

## Team Members

* Belnadino Mgimba
* Lloyd Donovan
* Thomas England

---

## Supervisor

Prof. Effie L-C. Law

---

## Disclaimer

HealthCare Plus is a research and educational prototype.

The AI assistant does not provide medical diagnoses, emergency advice, or professional healthcare recommendations. Users should always consult qualified healthcare professionals regarding medical concerns.

---

## License

This project was developed for academic and research purposes at Durham University.



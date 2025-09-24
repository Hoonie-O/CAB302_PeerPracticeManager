# Current Sprint Status & Priorities

## Sprint Execution Order
1. **IN REVIEW** (highest priority - finish first)
2. **IN PROGRESS** (complete these next)
3. **READY** (pull in order listed)
4. **BACKLOG** (future sprints)

## Current Board State

### IN REVIEW (Complete First)
- #62 Auth — Signup
- #60 Auth — Login  
- #61 Auth — Password reset

### IN PROGRESS (Finish These)
- #63 Profile — Availability status
- #64 Profile — Edit profile
- #65 Profile — Setting (Time and date)
- #66 Profile — Setting (Change password)
- #67 Profile — Logout
- #68 Study group — Create group
- #69 Study group — Join group

### READY (Next Tasks - Pull in Order)
- #80 Friends — Basic functions
- #81 Friends — Chat
- #75 Study group — Session (Tasks)
- #74 Study group — Session (New session)
- #76 Study group — Session (Notes)

### BACKLOG (Future Sprints)
- #70 Study group — Group sidebar
- #71 Study group — Admin group options
- #72 Study group — Member group options
- #73 Study group — Calendar (Group)
- #77 Study group — Group chat
- #78 Study group — Files

## Key Implementation Notes
- Auth system is mostly complete (Login/Signup controllers exist)
- Calendar functionality has foundation but needs edit/delete features per CALENDAR_TODO.md
- Profile features need to be built from scratch
- Study group features are the next major milestone
- Friends system will require new models and controllers

## Existing Code Assets
- User authentication flow (login, signup)
- Basic calendar with event management
- Navigation and controller factory pattern
- Email service for password reset
- BCrypt password hashing
- Mock DAO for development
### User Stories
### Account Storage in Database
As a **student**,  
I want my **account information to be stored securely in the database**,  
so that **I can log in and retrieve my sessions and notes across devices**.  

---

### Acceptance Criteria
- Given I register a new account, when I submit my details, then my data is saved in the database.  
- Given I log in, when the system checks my credentials, then they are validated against stored data.  
- Given I update account details (e.g., password), when I save changes, then the database reflects the update.  
- Given account data is stored, when another user logs in, then they cannot access my information.  
- Given I delete my account, when I confirm deletion, then all my account data is removed.  

---

### Sign Up Page
As a **new student**,  
I want a **sign-up page to create an account**,  
so that **I can access the app and start scheduling study sessions**.  

---

### Acceptance Criteria
- Given I am not registered, when I open the app, then I see an option to sign up.  
- Given I enter valid details, when I click "Sign Up", then a new account is created.  
- Given I leave required fields empty, when I try to sign up, then I see an error message.  
- Given I enter an already registered email, when I try to sign up, then I see a “user already exists” error.  
- Given my account is created, when I complete registration, then I am redirected to the main menu or login page.  

---

### Main Menu Functionality
As a **student**,  
I want a **main menu that lets me access core features**,  
so that **I can easily navigate between calendar, notes, and progress tracking**.  

---

### Acceptance Criteria
- Given I am logged in, when I open the app, then I see the main menu.  
- Given I am on the main menu, when I click "Calendar", then I am taken to the calendar view.  
- Given I am on the main menu, when I click "Notes", then I am taken to the notes section.  
- Given I am on the main menu, when I click "Progress", then I am taken to the progress logs.  
- Given I am on the main menu, when I click "Logout", then I am returned to the login screen.  

---

### Shared Notes Page
As a **student**,  
I want a **shared notes page for each study session**,  
so that **all participants can record and review what was covered together**.  

---

### Acceptance Criteria
- Given a session exists, when I open it, then I see an option to access the shared notes page.  
- Given I am on the notes page, when I add or edit notes, then changes are saved.  
- Given another participant adds or edits notes, when I refresh or revisit the page, then I see their changes.  
- Given I return to the session later, when I open the notes page, then previously saved notes are displayed.  

---

### Group Page Functionality
As a **student**,  
I want a **group page for my study team**,  
so that **I can see who’s in my group, manage members, and coordinate sessions together**.  

---

### Acceptance Criteria
- Given I am part of a group, when I open the group page, then I see the list of members.  
- Given I am a group creator/admin, when I invite a member, then they receive an invitation.  
- Given I am a group creator/admin, when I remove a member, then they no longer appear in the group list.  
- Given I am in a group, when I open the group page, then I see all upcoming group study sessions.  

---

### Study Session Join/Leave Functionality
As a **student**,  
I want to **join or leave study sessions**,  
so that **I can choose which sessions I participate in**.  

---

### Acceptance Criteria
- Given a session exists, when I click "Join", then I am added to the participant list.  
- Given I am a participant, when I click "Leave", then I am removed from the participant list.  
- Given I join a session, when I reopen the session details, then my name is shown as attending.  
- Given I leave a session, when I reopen the session details, then my name no longer appears in the participant list.  
- Given multiple students join a session, when I view the session, then all current participants are visible.  

---

### Study Session Details and Management Page
As a **student**,  
I want a **study session details and management page**,  
so that **I can view and manage information about each session in one place**.  

---

### Acceptance Criteria
- Given I open a study session, when the page loads, then I can see its title, date/time, location, description, and participants.  
- Given I am the session creator, when I click “Edit”, then I can update the session details and save changes.  
- Given I am the session creator, when I click “Delete”, then the session is removed from the calendar.  
- Given I am a participant, when I view the session, then I can join or leave the participant list.  
- Given I revisit the session page, when changes have been made, then the updated details are displayed.  

---

### User Role Access Control
As a **student or group admin**,  
I want **role-based access control**,  
so that **admins can manage groups and members while regular members have limited permissions**.  

---

### Acceptance Criteria
- Given I create a group, when it is made, then I am set as the **Admin** of that group.  
- Given I am an **Admin**, when I open the group page, then I can invite, remove, or change roles of members.  
- Given I am a **Member**, when I open the group page, then I can view sessions, join/leave sessions, and add notes, but not manage members or settings.  
- Given a group is private, when I am not a member, then I cannot access its content.  
- Given I attempt an action outside my role, when I try, then it is blocked and I see a clear message.  

---

### User Preference Settings Page
As a **student**,  
I want a **preferences/settings page**,  
so that **I can customise my account and app experience**.  

---

### Acceptance Criteria
- Given I am logged in, when I open the settings page, then I can view my account details and preferences.  
- Given I am on the settings page, when I change options (e.g., notifications, theme), then my changes are saved.  
- Given I update my profile information, when I save, then the updated info is shown across the app.  
- Given I click “Reset to default”, when I confirm, then all settings return to their default values.  
- Given I finish updating settings, when I return later, then my preferences are remembered and applied.  

---

### Account Login Function
As a **student**,  
I want to **log into my account**,  
so that **I can securely access my study sessions and notes**.  

---

### Acceptance Criteria
- Given I enter valid credentials, when I click “Log in”, then I am signed in and taken to the main menu.  
- Given I enter invalid credentials, when I try to log in, then I see an error message and remain on the login screen.  
- Given I have an active session, when I reopen the app, then I stay logged in (unless I logged out).  
- Given I click “Log out”, when the action completes, then my session ends and I return to the login screen.  

---

### User Authentication and Session Management
As a **student**,  
I want **authentication and session management** in the desktop app,  
so that **I can sign in securely and stay signed in until I log out or time out**.  

---

### Acceptance Criteria
- Given I enter valid credentials, when I sign in, then a session starts and I’m taken to the main menu.  
- Given I enter invalid credentials, when I try to sign in, then I see an error and remain on the login screen.  
- Given I’m not signed in, when I open any protected area, then I’m sent to the login screen.  
- Given “Remember me” is selected, when I relaunch the app, then I remain signed in (within the configured limit).  
- Given inactivity reaches the timeout, when I return, then I’m asked to re-authenticate before continuing.  
- Given I click “Log out”, when the action completes, then my session ends and I’m returned to the login screen.  
- Given I change my password, when the change succeeds, then any active session on this device requires re-login.  
- Given I close the app while signed in, when I relaunch within the active session window, then I’m still signed in.  
- Given the device is offline, when I start the app, then I can continue only if a valid cached session exists; otherwise sign-in fails with an offline message.  

---

### Password Reset Functionality
As a **student**,  
I want to **reset my password**,  
so that **I can regain access to my account if I forget it**.  

---

### Acceptance Criteria
- Given I’m on the login page, when I click “Forgot password?”, then I can enter my email to request a reset.  
- Given I provide a valid email, when I submit the request, then I receive a reset link or code.  
- Given I open the reset link or enter the code, when I provide a new password, then it replaces the old one.  
- Given I successfully reset my password, when I return to the login page, then I can sign in with the new password.  

---

### User Search and Discovery
As a **student**,  
I want to **search for users and groups**,  
so that **I can find classmates and study groups to collaborate with**.  

---

### Acceptance Criteria
- Given I open the search bar, when I type a name, username, or group, then matching results are shown.  
- Given I search for a user, when results appear, then I see their display name and basic info.  
- Given I search for a group, when results appear, then I see the group name and member count.  
- Given I find a user, when I click “Invite” or “Add”, then a request is sent to them.  
- Given I find a group, when I click “Request to join”, then my join request is sent.  
- Given no results match, when the search completes, then I see a “No results found” message.  

### Setup a Mock DAO and Respective User Management Class
As a **developer**, 
I want **a mock DAO and a UserManagement class**, 
so that **we can build and test user features without needing a real database yet**.

---

### Acceptance Criteria
- Given the app runs with a mock DAO, when I create, update, delete, or list users, then these actions work using in-memory storage (e.g., map or list).  
- Given I register a user, when the email already exists, then the operation fails with an appropriate error.  
- Given I use the UserManagement service, when I call register, find, update, or remove, then the request is validated and delegated to the DAO (no DB logic in service).  
- Given I provide invalid input (e.g., empty name, invalid email format), when I call register or update, then the operation is rejected with an error.  
- Given the mock DAO follows the UserDao interface, when I swap it with a real DAO later, then no external code changes are required.  
- Given unit tests are run, when CRUD operations, duplicate emails, and user-not-found cases are tested, then they all pass.  
- Given documentation exists, when a developer reads it, then they understand how to run the tests and why the mock is used.  


### Password Hashing
As a **user**,
I want **my password stored securely**, 
so that **my account stays safe even if the database is leaked**.

---

### Acceptance Criteria
- Given a user registers, when their password is stored, then it is hashed using a secure algorithm (e.g., bcrypt or Argon2) and never saved in plain text.  
- Given the hashing utility is used, when the same password is hashed twice, then the results differ due to unique salts.  
- Given a user logs in, when the password is verified, then the correct password returns true and an incorrect password returns false.  
- Given configuration exists, when the work factor (e.g., cost rounds) is updated in settings, then the hashing adjusts without requiring code changes.  
- Given logging is enabled, when passwords are processed, then no plain text password ever appears in logs, exceptions, or API responses.  
- Given the system evolves, when hashing algorithms or cost factors change, then documentation describes how to migrate existing hashes.  
- Given tests are run, when hashing and verification are executed, then they confirm salting, correct verification, and rejection of invalid passwords.  

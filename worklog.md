# Development Work Log

## Previous Sprint - Session Management Implementation
### Session Models & Business Logic:
1. **`PeerPractice/src/main/java/com/cab302/peerpractice/Model/Session.java`**
    - Basic study session with organiser, participants, timing
    - Handles participant management (can't remove organiser)
    - Simple validation for start/end times
    - Supports session metadata (title, description, topic)

2. **`PeerPractice/src/main/java/com/cab302/peerpractice/Model/SessionStatus.java`**
    - Enum with `PLANNED`, `ACTIVE`, `COMPLETED`, `CANCELLED` states
    - Provides some  session life cycle management

3. **`PeerPractice/src/main/java/com/cab302/peerpractice/Model/SessionManager.java`**
    - Manages session creation and retrieval
    - Methods for getting user sessions, upcoming sessions
    - Basic in-memory storage for now
    - Handles session validation and business rules

### User Session & Authentication:
4. **`PeerPractice/src/main/java/com/cab302/peerpractice/Model/UserSession.java`**
    - Manages current user login state
    - Thread-safe singleton pattern
    - Session timeout and cleanup functionality
    - Integration with logout functionality

### Test Files:
5. **`PeerPractice/src/main/test/SessionTest.java`**
    - 5 unit tests covering basic session functionality
    - Tests participant management, validation, status handling
    - Ensures business rules are enforced correctly

6. **`PeerPractice/src/main/test/LogoutFunctionalityTest.java`**
    - 9 unit tests for logout functionality
    - Tests `UserSession`, `UserManager` integration, security, thread safety
    - Comprehensive coverage of logout scenarios

7. **`PeerPractice/src/main/test/MainMenuControllerLogoutTest.java`**
    - 6 unit tests for controller logout logic
    - Tests profile initialisation, navigation, session state
    - UI controller behaviour validation

### Controller Integration:
8. **`PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/MainMenuController.java`**
    - Added logout confirmation dialog
    - Enhanced error handling with `handleLogoutError()` method
    - Added success message with `showLogoutSuccessMessage()`
    - Dynamic user profile display in `initializeUserProfile()`
    - Added FXML fields for `userNameLabel` and `userUsernameLabel`

### UI Updates:
9. **`PeerPractice/src/main/resources/com/cab302/peerpractice/mainmenu-view.fxml`**
    - Changed logout button action from onBackToLogin to onLogout
    - Added `fx:id` attributes to user name and username labels for dynamic content
    - Improved user profile display integration

### Dependency Injection & Configuration:
10. **`PeerPractice/src/main/java/com/cab302/peerpractice/AppContext.java`**
    - Added `SessionManager` to dependency injection
    - Fixed naming conflict with `jakarta.mail.Session`
    - Proper service registration and life cycle management

11. **`PeerPractice/src/main/java/com/cab302/peerpractice/Model/MailService.java`**
    - Used fully qualified names to avoid Session class conflict
    - Maintained email functionality while resolving naming conflicts

### Build Configuration:
12. **`PeerPractice/pom.xml`**
    - Added build-helper-maven-plugin to support custom test directory
    - Added maven-surefire-plugin configuration for test execution
    - Enables running tests from src/main/test/ location
    - Proper Maven project structure support

## Current Sprint - Calendar System Rework (Event → Session/Availability)

Alright so this was a pretty massive sprint. Basically took the whole Event class system and completely reworked it because we already had a better Session class implementation. The goal was to make the calendar support both study sessions and personal availability scheduling.

### Major Architectural Changes:
1. **Event System Redesign**: Completely ditched the Event class for calendar stuff and replaced it with a dual system - Sessions for actual study group coordination and Availability for personal scheduling. Way cleaner architecture tbh.

### New Model Classes (the fun stuff):
2. **`Availability.java`**
    - This handles personal availability tracking with recurring patterns
    - You can set daily, weekly, monthly recurring schedules which is pretty neat
    - Color-coded availability blocks so you can actually see what's what
    - Each availability is tied to a user and has descriptions

3. **Enhanced `Session.java`**
    - Extended the existing session class to work with the calendar
    - Added subject, location, color labels, max participants - all the good stuff
    - Now has proper calendar integration with time management
    - Participant limits so sessions don't get overcrowded
    - Status tracking that actually works

4. **`SessionCalendarManager.java`**
    - This is the brain of the calendar session operations  
    - Does all the date range queries and filtering
    - Handles session CRUD operations specifically for calendar display
    - Plays nice with the UI components

5. **`SessionCalendarStorage.java`**
    - In-memory persistence layer for calendar sessions
    - Date-based filtering and retrieval (way faster than the old system)
    - User-specific session queries
    - Week/month range operations for different calendar views

6. **`AvailabilityManager.java`**
    - Manages the whole availability lifecycle
    - Can query multiple users' availability for friend views (planning ahead)
    - Handles recurring pattern logic which was actually kinda tricky
    - Week-based availability coordination

7. **`AvailabilityStorage.java`**
    - Storage layer for all the availability data
    - User and date filtering capabilities
    - Friend availability aggregation for when you want to see when everyone's free
    - Recurring pattern storage with proper validation

### Controller Overhaul (this took forever):
8. **Complete `CalendarController.java` Rewrite**
    - Dual view toggle system - you can switch between Sessions and Availability views
    - Context-aware dialog creation based on what view you're in
    - Session creation dialogs with all the features (subject, location, participants, the works)
    - Availability creation with recurring pattern support
    - View-specific item display so you see the right stuff
    - Integrated delete functionality for both types
    - Actually decent UI layout and user experience now

### UI/FXML Updates:
9. **`calendar-view.fxml`**
    - Added toggle button controls for switching views
    - Proper layout spacing so it doesn't look like garbage
    - Integration of all the new UI components
    - Some accessibility improvements because why not

### Dependency Management:
10. **Updated `AppContext.java`**
    - Added SessionCalendarManager and AvailabilityManager to dependency injection
    - Removed EventManager completely (finally!)
    - Proper service lifecycle management
    - Still integrates with the existing user session system

### Testing (the boring but necessary stuff):
11. **`AvailabilityTest.java`**
    - 4 unit tests covering the Availability class thoroughly  
    - Tests creation, validation, recurring patterns, user associations
    - Edge case handling for when people input weird data
    - Null safety because crashes are bad

12. **`SessionCalendarManagerTest.java`**
    - 4 unit tests for calendar session management
    - CRUD operations, date filtering, session counting
    - Integration testing with the storage layer
    - Makes sure business rules are actually enforced

### The Big Cleanup - EventManager Replacement:
13. **EventManager Complete Removal**
    - Nuked EventManager from AppContext completely
    - Removed all Event-related methods from User class (addEvent, getEvents)
    - Cleaned up test references to old Event functionality  
    - Calendar system now runs 100% on SessionCalendarManager and AvailabilityManager
    - No more dual systems - it's clean now

### Test Fixes (the annoying part):
14. **Fixed All Test Failures**
    - **UserTest failures (39 tests)**: Fixed User class validation - proper name patterns, bio length limits, username constraints with Unicode support
    - **UserManagerTest failures (3 tests)**: Fixed password validation requirements, email validation, Unicode name support
    - **LogoutFunctionalityTest (2 tests)**: Fixed authentication requirements for proper password format and username length
    - **MainMenuControllerLogoutTest (1 test)**: Fixed username length requirements
    - **All 120 tests now passing** - no failures, no errors

### User Class Validation Improvements:
15. **Enhanced User.java Validation**
    - Added proper validation for first/last names (supports Unicode characters now)
    - Username validation with 6+ character requirement and proper character set
    - Email validation that actually works
    - Bio length limits (200 characters) with proper error handling
    - Friend list initialization to prevent null pointer exceptions
    - Input trimming and null checking for all setters

## Key Achievements This Sprint:
- **Dual Calendar System**: Toggle between Sessions and Availability views works perfectly
- **EventManager Fully Replaced**: SessionManager now completely handles all calendar functionality
- **Enhanced Session Management**: Full-featured study session creation with participants, subjects, locations
- **Personal Availability Tracking**: User-specific availability with recurring patterns
- **All Tests Passing**: Fixed every single test failure (120/120 tests passing)
- **Clean Architecture**: Manager-Storage pattern for both session types
- **Professional UI/UX**: Proper dialogs, confirmations, view-specific displays
- **Robust Validation**: User input validation that handles edge cases and Unicode
- **System Integration**: Everything works together with existing auth and user management
- **Performance**: In-memory storage optimized for calendar operations

## What Actually Works Now:
- Calendar system runs entirely on SessionCalendarManager and AvailabilityManager
- You can create study sessions with full details (subject, location, max participants)  
- Personal availability scheduling with recurring patterns (daily/weekly/monthly)
- Toggle between Session view and Availability view seamlessly
- All user input validation works properly (names, emails, usernames, bios)
- Proper error handling and user feedback
- All tests pass without any failures or errors
- Application compiles and runs without issues

## Files Modified/Created This Sprint:
**New Files (7):**
- Availability.java (personal availability scheduling)
- AvailabilityManager.java (availability lifecycle management)  
- AvailabilityStorage.java (availability data storage)
- SessionCalendarManager.java (calendar session operations)
- SessionCalendarStorage.java (calendar session storage)
- AvailabilityTest.java (availability unit tests)
- SessionCalendarManagerTest.java (session calendar tests)

**Major Rewrites (2):**
- CalendarController.java (complete rewrite for dual view system)
- User.java (enhanced validation and initialization)

**Updated Files (3):**
- Session.java (enhanced for calendar integration)
- calendar-view.fxml (added toggle buttons)
- AppContext.java (removed EventManager, added new managers)

**Test Fixes (4):**
- UserTest.java (fixed validation test expectations)
- UserManagerTest.java (fixed password/email validation tests)
- LogoutFunctionalityTest.java (fixed authentication requirements)
- MainMenuControllerLogoutTest.java (fixed username requirements)

## Next Sprint Goals:
- Maybe add friend availability viewing
- Could implement drag-and-drop for calendar items
- Might need to add persistence layer (database) eventually
- Could add email notifications for sessions
- Friend integration with availability coordination
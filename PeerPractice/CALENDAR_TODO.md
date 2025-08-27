# Calendar Features - TODO for Teammate

## What I've done done
- ✅ Basic calendar display with monthly view
- ✅ Add new events with title, time, description, and color labels
- ✅ View events in a simple popup list
- ✅ Event storage and management foundation

## TODO

### 1. Edit functionality
**Location**: `CalendarController.java`
- Need to add "Edit" button to each event in the event list dialog
- Also need to create a new method `showEditEventDialog(Event event)` similar to `showAddEventDialog`
- Wire up the edit button to open the dialog with pre-filled values
- Update the EventManager to handle event edits
- [DONE]

### 2. Delete unctionality  
**Location**: `CalendarController.java` 
- Need to add "Delete" button next to edit button
- Show a confirmation dialog before deleting
- Remove the event from storage and refresh the calendar view

### 3. Improvements
**Location**: `EventManager.java` lines 56-62
- Use the`updateEvent(Event oldEvent, Event newEvent)` method i made
- Maybe add unique IDs to events (can use UUID field in Event class). Just makes finding and updating specific events more reliable

### 4. Optional Enhancements
- Add keyboard shortcuts (Enter to save, Escape to cancel)
- Better error messages
- Add drag-and-drop to move events between dates
- Week and day view options

## Code Structure Notes
- Used modular mvc pattern (https://www.geeksforgeeks.org/system-design/mvc-design-pattern/) | (https://www.youtube.com/watch?v=dTVVa2gfht8)
- `EventStorage` is in-memory but i made it so it can be easily replaced with database storage later
- Colour labels are handled as strings but could be converted to an enum
- For UI stuff, kinda just followed Hoonie's javafx patterns

## Getting Started
1. Look at the existing `showAddEventDialog()` method just to use as a reference
2. The event list dialog already displays events nicely, just really needs the buttons
3. U can test ur changes by running `./mvnw clean javafx:run`
4. Follow the same coding style, just for consistency, makes it easier in future
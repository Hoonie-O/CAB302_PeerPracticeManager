package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.DAOs.SessionCalendarDAO;
import com.cab302.peerpractice.Model.DAOs.SessionTaskDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.SessionTask;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.SessionCalendarManager;
import com.cab302.peerpractice.Model.Managers.SessionManager;
import com.cab302.peerpractice.Model.Managers.SessionTaskManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Demonstration of session task functionality working end-to-end
 * using the real DAOs but safely (creates & deletes test data).
 */
public class SessionTaskDemoTest {

    public static void main(String[] args) {
        System.out.println("=== Session Task Functionality Demo ===");

        try {
            // Setup real DAOs and managers
            UserDAO userDAO = new UserDAO();
            SessionCalendarManager calendarManager = new SessionCalendarManager(new SessionCalendarDAO(userDAO));
            SessionManager sessionManager = new SessionManager(calendarManager);
            SessionTaskManager taskManager = new SessionTaskManager(new SessionTaskDAO(userDAO), sessionManager);

            // Create users (and persist them if your UserDAO requires)
            User organizer = new User("John", "Doe", "johndoe", "john@example.com", "hashedpass", "QUT");
            User participant1 = new User("Jane", "Smith", "janesmith", "jane@example.com", "hashedpass", "QUT");
            User participant2 = new User("Bob", "Wilson", "bobwilson", "bob@example.com", "hashedpass", "QUT");
            userDAO.addUser(organizer);
            userDAO.addUser(participant1);
            userDAO.addUser(participant2);

            System.out.println("✅ Created users with IDs: " +
                    organizer.getUserId() + ", " + participant1.getUserId() + ", " + participant2.getUserId());

            // Create session
            Session session = sessionManager.createSession(
                    "Study Group Session", organizer,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(1).plusHours(2)
            );
            session.addParticipant(participant1);
            session.addParticipant(participant2);

            System.out.println("✅ Created session: " + session.getTitle() +
                    " with ID: " + session.getSessionId());
            System.out.println("   Participants: " + session.getParticipantCount());

            // Create tasks
            SessionTask task1 = taskManager.createTask(
                    session.getSessionId(),
                    "Prepare presentation slides",
                    LocalDateTime.now().plusDays(1).minusHours(1),
                    participant1.getUserId(),
                    organizer.getUserId()
            );
            SessionTask task2 = taskManager.createTask(
                    session.getSessionId(),
                    "Research topic background",
                    LocalDateTime.now().plusDays(1).minusHours(2),
                    participant2.getUserId(),
                    organizer.getUserId()
            );
            System.out.println("✅ Created tasks: " + task1.getTitle() + ", " + task2.getTitle());

            // List tasks for session
            List<SessionTask> tasks = taskManager.getSessionTasks(session.getSessionId());
            System.out.println("✅ Session has " + tasks.size() + " tasks");

            // Update a task
            SessionTask updated = taskManager.updateTask(
                    task1.getTaskId(),
                    "Prepare presentation slides (updated)",
                    LocalDateTime.now().plusDays(1).minusMinutes(30),
                    participant1.getUserId(),
                    organizer.getUserId()
            );
            System.out.println("✅ Updated task: " + updated.getTitle());

            // Mark task completed
            boolean completed = taskManager.markTaskCompleted(task2.getTaskId(), participant2.getUserId());
            System.out.println("✅ Task 2 completed: " + completed);

            // Validation checks
            try {
                User outsider = new User("Alice", "Brown", "alicebrown", "alice@example.com", "hashedpass", "QUT");
                taskManager.createTask(
                        session.getSessionId(),
                        "Invalid outsider task",
                        LocalDateTime.now().plusDays(1),
                        outsider.getUserId(),
                        organizer.getUserId()
                );
                System.out.println("❌ Validation failed - outsider assigned");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Assignee validation works: " + e.getMessage());
            }

            try {
                taskManager.createTask(
                        session.getSessionId(),
                        "Past deadline task",
                        LocalDateTime.now().minusDays(1),
                        participant1.getUserId(),
                        organizer.getUserId()
                );
                System.out.println("❌ Validation failed - past deadline allowed");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Deadline validation works: " + e.getMessage());
            }

            // Delete a task
            boolean deleted = taskManager.deleteTask(task1.getTaskId(), organizer.getUserId());
            System.out.println("✅ Task deleted: " + deleted);

            // Final summary
            List<SessionTask> finalTasks = taskManager.getSessionTasks(session.getSessionId());
            System.out.println("=== Final Summary ===");
            for (SessionTask t : finalTasks) {
                System.out.println("- " + t.getTitle() + " (completed: " + t.isCompleted() + ")");
            }

            // 🎉 Success
            System.out.println("\n🎉 End-to-end session task demo successful!");

            // Cleanup: delete tasks + session + users
            for (SessionTask t : finalTasks) taskManager.deleteTask(t.getTaskId(), organizer.getUserId());
            sessionManager.removeSession(session);
            userDAO.deleteUser(organizer.getUserId());
            userDAO.deleteUser(participant1.getUserId());
            userDAO.deleteUser(participant2.getUserId());

        } catch (Exception e) {
            System.err.println("❌ Error during demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

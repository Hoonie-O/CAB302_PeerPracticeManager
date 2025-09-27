# Architecture Patterns and Design Decisions

## Core Architecture

### Dependency Injection Pattern
- **AppContext**: Central container holding all services (UserManager, EventManager, MailService, etc.)
- **Controller Factory**: Automatic injection of AppContext and Navigation into all controllers
- **Constructor Injection**: All controllers receive dependencies via constructor

### MVC Pattern Implementation
- **Model**: Business logic classes (User, Event, UserManager, EventManager)
- **View**: FXML files with declarative UI layout
- **Controller**: JavaFX controllers handling user interactions and view updates

### Navigation System
- **Centralized Navigation**: Single Navigation class manages all view transitions
- **View Enum**: Defines available views with FXML file mappings
- **Stage Management**: Single stage with scene switching

## Data Access Patterns

### Repository/DAO Pattern
- **IUserDAO**: Interface defining data access operations
- **MockDAO**: In-memory implementation for development
- **Future**: Easy to replace with database implementation

### Session Management
- **UserSession**: Singleton-like pattern for current user state
- **Context Sharing**: Session available through AppContext injection

## Security Patterns
- **Password Hashing**: BCrypt implementation with salt
- **Hasher Interface**: Abstraction allowing different hashing strategies
- **Input Validation**: Centralized in UserManager with regex patterns

## UI Patterns

### FXML Controller Binding
```java
// Standard controller pattern
public class LoginController extends BaseController {
    @FXML private TextField IDField;
    // Constructor injection via controller factory
    public LoginController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }
}
```

### Event Handling Pattern
```java
@FXML
private void initialize() {
    // Bind UI state to data
    loginButton.disableProperty().bind(
        IDField.textProperty().isEmpty()
        .or(passwordField.textProperty().isEmpty())
    );
    // Set event handlers
    loginButton.setOnAction(e -> login());
}
```

## Error Handling Strategy
- **Custom Exceptions**: Domain-specific exceptions (DuplicateEmailException, etc.)
- **Exception Hierarchy**: All inherit from RuntimeException
- **UI Error Display**: Error messages shown via Label components
- **Graceful Degradation**: Application continues running after validation errors

## Future Architecture Considerations
- Database integration will replace MockDAO
- Authentication tokens/sessions for security
- RESTful API layer for potential web client
- Event-driven architecture for real-time features (chat, notifications)
# Code Style and Conventions

## Java Conventions
- **Naming**: PascalCase for classes, camelCase for methods/variables
- **Package Structure**: `com.cab302.peerpractice.<module>`
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: No strict limit observed, but reasonable line breaks
- **Access Modifiers**: Use appropriate visibility (private, protected, public)

## Architecture Patterns
- **Controller Pattern**: All controllers extend `BaseController`
- **Constructor Injection**: Controllers receive `AppContext` and `Navigation`
- **Controller Factory**: Automatic dependency injection via reflection
- **FXML Binding**: Controllers bound to FXML views via `fx:controller`

## File Organization
- **Controllers**: One controller per view, named `<View>Controller.java`
- **Models**: Business logic classes in Model package
- **Views**: FXML files in resources with matching controller names
- **Exceptions**: Custom exceptions in `Exceptions` package
- **Tests**: Unit tests in `src/main/test/` (note: not standard Maven structure)

## Method Naming Patterns
- **Event Handlers**: `private void methodName()` for FXML events
- **Validation**: `private static void validateX(String value)` pattern
- **Business Logic**: Descriptive method names like `authenticate()`, `signUp()`

## JavaFX Patterns
- **FXML Injection**: `@FXML` annotations for UI components
- **Event Binding**: Set handlers in `initialize()` method
- **Property Binding**: Use JavaFX property binding for UI state
- **Scene Navigation**: Use central `Navigation` class for view switching

## Error Handling
- **Custom Exceptions**: Domain-specific exceptions (DuplicateEmailException, etc.)
- **Validation**: Input validation in UserManager with descriptive messages
- **UI Feedback**: Error messages displayed via Label components

## Database Pattern
- **DAO Interface**: `IUserDAO` interface for data access abstraction
- **Mock Implementation**: `MockDAO` for development/testing
- **Repository Pattern**: Clean separation between business logic and data access
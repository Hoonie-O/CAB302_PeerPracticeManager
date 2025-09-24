# Task Completion Checklist

## When a Development Task is Completed

### 1. Code Quality
- [ ] Follow existing architecture patterns (MVC, dependency injection)
- [ ] Extend `BaseController` for new controllers
- [ ] Use appropriate error handling and validation
- [ ] Follow Java naming conventions (camelCase, PascalCase)
- [ ] Add proper JavaDoc comments for complex methods

### 2. Testing Requirements
```bash
# Run tests to ensure nothing is broken
cd PeerPractice
./mvnw test
```
- [ ] Existing tests still pass
- [ ] Add unit tests for new business logic in UserManager, EventManager
- [ ] Test controllers with mock data if complex logic added

### 3. Build Verification
```bash
# Ensure clean build
./mvnw clean compile

# Test application startup
./mvnw clean javafx:run
```
- [ ] Application compiles without warnings (ignore module name warning)
- [ ] Application starts and navigates properly
- [ ] New features work as expected in UI

### 4. FXML and UI Guidelines
- [ ] Follow existing FXML structure and styling
- [ ] Use consistent spacing and layout patterns
- [ ] Ensure proper `fx:controller` binding
- [ ] Test UI responsiveness and accessibility basics

### 5. Git Workflow
```bash
# Branch naming convention
git checkout -b feature/<short-description>-#<card-number>

# Commit message format (Conventional Commits)
git commit -m "feat(auth): implement signup validation (#62)"
git commit -m "fix(calendar): resolve event deletion bug (#65)"
git commit -m "refactor(model): improve user validation logic"
```

### 6. Pull Request Preparation
- [ ] Branch follows naming convention: `feature/<name>-#<id>`
- [ ] Commits use conventional commit format
- [ ] Include screenshots/gifs if UI changes
- [ ] Document any database schema changes needed
- [ ] Update relevant documentation if significant changes

### 7. Definition of Done Verification
- [ ] Feature works as specified in acceptance criteria
- [ ] Error states and edge cases handled
- [ ] UI is accessible (basic keyboard navigation)
- [ ] Code is consistent with existing patterns
- [ ] No breaking changes to existing functionality
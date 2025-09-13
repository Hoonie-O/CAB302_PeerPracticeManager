# Suggested Commands for CAB302 Peer Practice Manager

## Build & Run Commands
```bash
# Navigate to project directory
cd PeerPractice

# Clean and compile
./mvnw clean compile

# Run the application
./mvnw clean javafx:run

# Run tests
./mvnw test

# Clean build artifacts
./mvnw clean
```

## Development Workflow Commands
```bash
# Check Maven version and Java setup
./mvnw --version

# Compile only (faster for development)
./mvnw compile

# Package the application
./mvnw package

# Install to local Maven repository
./mvnw install
```

## Git Commands (Standard)
```bash
# Check status
git status

# Create feature branch
git checkout -b feature/<feature-name>-#<card-number>

# Stage changes
git add .

# Commit with conventional format
git commit -m "feat(auth): implement signup flow (#62)"

# Push branch
git push -u origin feature/<feature-name>-#<card-number>
```

## File System Navigation
```bash
# List project structure
find PeerPractice/src -type f -name "*.java" | head -20

# Search for specific code patterns
grep -r "authenticate" PeerPractice/src/main/java

# Find FXML files
find PeerPractice/src/main/resources -name "*.fxml"
```

## Key Project Paths
- **Main Application**: `PeerPractice/src/main/java/com/cab302/peerpractice/PeerPracticeApplication.java`
- **Controllers**: `PeerPractice/src/main/java/com/cab302/peerpractice/Controllers/`
- **Models**: `PeerPractice/src/main/java/com/cab302/peerpractice/Model/`
- **Views (FXML)**: `PeerPractice/src/main/resources/com/cab302/peerpractice/`
- **Tests**: `PeerPractice/src/main/test/`
- **Build Output**: `PeerPractice/target/`
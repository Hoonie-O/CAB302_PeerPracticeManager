# Development Environment Setup

## System Requirements
- **Java**: OpenJDK 21 (Amazon Corretto 21.0.8)
- **Maven**: 3.8.5 (included via wrapper)
- **Platform**: Linux (tested on Arch Linux 6.16.7)
- **IDE**: IntelliJ IDEA recommended (project has .idea config)

## Project Structure
```
CAB302_PeerPracticeManager/
├── address-book/          # Separate module (legacy?)
├── PeerPractice/         # Main application
│   ├── .mvn/             # Maven wrapper
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/     # Java source code
│   │   │   └── resources/ # FXML files, assets
│   │   └── test/         # Test files (non-standard location)
│   ├── target/           # Build output (ignored)
│   ├── pom.xml           # Maven configuration
│   ├── mvnw              # Maven wrapper script
│   └── CALENDAR_TODO.md  # Development notes
└── .claude/              # AI assistant configuration
```

## Maven Configuration
- **Java Version**: Source/Target 21
- **JavaFX Version**: 21.0.6
- **JUnit Version**: 5.12.1
- **Main Class**: `com.cab302.peerpractice.PeerPracticeApplication`

## IDE Setup Notes
- **Module System**: Uses Java modules (module-info.java)
- **JavaFX**: Configured with Maven plugin for easy running
- **Dependencies**: All managed through Maven, no manual classpath setup needed

## Common Issues
- **Module Warning**: "module name component cab302 should avoid terminal digits" (can ignore)
- **Test Location**: Tests in `src/main/test/` instead of standard `src/test/java/`
- **jbcrypt Warning**: "Required filename-based automodules detected" (can ignore)

## Development Workflow
1. Use Maven wrapper (`./mvnw`) instead of system Maven
2. Run `./mvnw clean javafx:run` for development testing
3. Use `./mvnw test` for running unit tests
4. Build artifacts go to `target/` directory (auto-cleaned)

## Git Configuration
- Standard `.gitignore` for Maven + Java + IDE files
- Ignores `target/`, `.idea/`, IDE temp files
- Includes Maven wrapper files in version control
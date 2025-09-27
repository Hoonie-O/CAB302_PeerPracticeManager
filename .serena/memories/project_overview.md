# CAB302 Peer Practice Manager - Project Overview

## Purpose
A Java-based peer practice management system for students to organize study groups, schedule sessions, manage tasks, and collaborate on academic work. The application supports user authentication, calendar functionality, study group management, friend systems, and session coordination.

## Technology Stack
- **Language**: Java 21
- **UI Framework**: JavaFX 21.0.6 with FXML
- **Build Tool**: Maven 3.8.5 
- **Testing**: JUnit Jupiter 5.12.1
- **Password Hashing**: BCrypt (jbcrypt 0.4)
- **Email**: Jakarta Mail API 2.1.4 with Angus Mail 2.0.4
- **Additional UI**: ControlsFX, FormsFX, ValidatorFX, Ikonli, BootstrapFX, TilesFX

## Architecture Pattern
- **MVC Pattern**: Model-View-Controller separation
- **Dependency Injection**: Constructor injection via controller factory
- **Repository Pattern**: DAO interfaces with mock implementations
- **Session Management**: UserSession for current user state
- **Navigation**: Centralized view navigation system

## Core Components
- **AppContext**: Central dependency injection container
- **Navigation**: View management and navigation controller
- **BaseController**: Base class for all controllers with shared dependencies
- **UserManager**: User authentication and registration logic
- **EventManager**: Calendar and event management
- **MailService**: Email functionality for password reset
- **MockDAO**: In-memory data persistence (development)
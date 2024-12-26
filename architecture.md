# Book Tracker Application Architecture

## 1. Overall Architecture

The application follows a layered architecture pattern with Model-View-Controller (MVC) design:

```
com.booktracker/
├── ui/           # Presentation Layer (View + Controller)
│   ├── MainWindow
│   ├── dialogs/
│   └── panels/
├── service/      # Business Logic Layer
│   ├── BookService
│   ├── UserService
│   └── ReadingProgressService
├── dao/          # Data Access Layer
│   ├── DatabaseManager
│   ├── BookDao
│   ├── UserDao
│   └── ReadingProgressDao
└── model/        # Domain Models
    ├── Book
    ├── User
    ├── ReadingProgress
    └── ReadingSession

```

## 2. Component Details

### 2.1 Model Layer (Domain Objects)

#### Book
```java
public class Book {
    private Long id;
    private String title;
    private String author;
    private int totalPages;
    private LocalDateTime dateAdded;
    // getters, setters
}
```

#### User
```java
public class User {
    private Long id;
    private String username;
    private String databasePath;
    // getters, setters
}
```

#### ReadingProgress
```java
public class ReadingProgress {
    private Long id;
    private Long bookId;
    private Long userId;
    private int currentPage;
    private double progressPercentage;
    private LocalDateTime lastReadDate;
    // getters, setters
}
```

#### ReadingSession
```java
public class ReadingSession {
    private Long id;
    private Long bookId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int pagesRead;
    // getters, setters
}
```

### 2.2 Data Access Layer (DAO)

#### DatabaseManager
- Manages database connections
- Implements connection pooling
- Handles database file creation per user
- Manages database schema creation and updates

#### BookDao
- CRUD operations for books
- Book search and filtering
- Book metadata management

#### UserDao
- User management operations
- Database file path management
- User authentication

#### ReadingProgressDao
- Progress tracking operations
- Reading session management
- Statistics data retrieval

### 2.3 Service Layer

#### BookService
- Book management business logic
- Book validation
- Library management operations

#### UserService
- User management business logic
- User database initialization
- User session management

#### ReadingProgressService
- Progress tracking logic
- Statistics calculation
- Reading analytics

### 2.4 UI Layer

#### MainWindow
- Main application window
- Menu bar management
- Layout management

#### Dialogs
- BookDialog: Add/Edit book information
- ReadingSessionDialog: Record reading sessions
- ProgressDialog: Update reading progress
- StatisticsDialog: View reading statistics

#### Panels
- BookListPanel: Display book library
- ProgressPanel: Show reading progress
- StatisticsPanel: Display reading statistics

## 3. Database Schema

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    database_path VARCHAR(255) NOT NULL
);

CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    total_pages INT NOT NULL,
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reading_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT,
    user_id BIGINT,
    current_page INT,
    progress_percentage DOUBLE,
    last_read_date TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE reading_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    book_id BIGINT,
    user_id BIGINT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    pages_read INT,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 4. Key Design Patterns

1. **Singleton Pattern**
   - Used for DatabaseManager to manage database connections
   - Ensures single instance of connection pool

2. **DAO Pattern**
   - Separates data access logic from business logic
   - Provides consistent interface for data operations

3. **MVC Pattern**
   - Separates UI, business logic, and data access
   - Improves maintainability and testability

4. **Observer Pattern**
   - Used for UI updates when data changes
   - Implements event handling in Swing components

5. **Factory Pattern**
   - Creates database connections
   - Manages UI component creation

## 5. Multi-User Support

- Each user has a separate H2 database file
- Database files stored in user-specific directories
- Connection management handles multiple database files
- User switching mechanism in UI

## 6. Error Handling

- Custom exceptions for each layer
- Proper error messages in UI
- Transaction management for data consistency
- Logging system for debugging

## 7. Performance Considerations

- Connection pooling for database operations
- Lazy loading of UI components
- Caching of frequently accessed data
- Batch processing for bulk operations

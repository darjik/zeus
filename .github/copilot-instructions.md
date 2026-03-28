# Inventory Management System (IMS) - Development Instructions

---
applyTo: "**/*.java"
---

# Checkstyle Guidelines

## General Formatting

- **Line Length**: Maximum line length is 200 characters
    - Exception: Import statements, package declarations, and URLs can exceed this limit
- **File Encoding**: Use UTF-8 encoding for all files
- **Tab Characters**: Do not use tab characters; use spaces for indentation
- **Indentation**: Use 4 spaces for indentation
    - Basic offset: 4 spaces
    - Case indentation in switch statements: 4 spaces
    - Array initialization indentation: 4 spaces

## Whitespace Rules

- **Whitespace Around Operators**: Include space before and after operators (=, +, -, etc.)
- **Empty Blocks**: Empty blocks must contain a comment or statement
- **Line Separators**: Use empty lines between:
    - Class definitions
    - Methods
    - Static initialization blocks
    - Instance initialization blocks
- **Separator Placement**:
    - Place dots (.) at the beginning of a new line when breaking lines
    - Place commas (,) at the end of the line

## Naming Conventions

- **Package Names**:
    - All lowercase
    - Follow pattern: `^[a-z]+(\.[a-z][a-z0-9]*)*$`
    - Example: `com.example.project`

- **Type Names (Classes, Interfaces)**:
    - UpperCamelCase
    - Example: `CustomerService`

- **Member Variable Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9]*$`
    - Example: `customerName`

- **Method Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9_]*$`
    - Example: `calculateTotal`

- **Parameter Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9]*$`
    - Example: `userName`

- **Local Variable Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9]*$`
    - Example: `tempValue`

- **Type Parameter Names**:
    - Should either be:
        - A single uppercase letter (possibly followed by a single digit), e.g., `T`, `E`, `V`, `T1`
        - An uppercase letter followed by more letters and digits and ending with 'T', e.g., `ElementT`, `ResponseT`

## Code Structure

- **One Top-Level Class**: Only one top-level class per file
- **Imports**:
    - No wildcard/star imports (`import package.*`)
- **Braces**:
    - Required for all control structures (if, for, while, do, etc.) even for single statements
    - Right braces should be alone on the line for class definitions, method definitions, constructors, etc.
- **One Statement Per Line**: Do not put multiple statements on the same line
- **Multiple Variable Declarations**: Do not declare multiple variables in the same declaration
- **Modifiers Order**: Follow the standard order for modifiers (e.g., public static final, not final static public)

## Flow Control

- **Switch Statements**: All switch statements must have a default case
- **Fall Through**: Cases in switch statements must not fall through (add a comment `// fall through` if intentional)

## Other Rules

- **No Finalizers**: Do not use finalizers (override `close()` instead)
- **Array Style**: Use Java style arrays (`String[] args`, not `String args[]`)
- **Uppercase "L"**: Use uppercase "L" for long literals (`long value = 100000L`)
- **Generic Whitespace**: No spaces inside generic brackets
- **Overloaded Methods**: Overloaded methods should be grouped together
- **Variable Declaration Distance**: Variables should be declared close to their usage (within 10 lines)
- **Abbreviations**: Limit abbreviation length in names to 4 characters
- **Unicode Characters**: Avoid using escaped Unicode characters unless necessary

## Suppressing Warnings

If you need to suppress a specific warning for a valid reason:

1. Use the `@SuppressWarnings` annotation for the specific check
2. Add entries to the `suppressions.xml` file for broader suppressions

## Common Issues and Solutions

- **Line Too Long**: Break long lines at logical points or refactor
- **Whitespace Issues**: Run code formatting in your IDE
- **Naming Convention Violations**: Rename variables/methods/classes according to the patterns
- **Missing Braces**: Always add braces around code blocks even for single statements
- **Import Order**: Organize imports in your IDE


## Rules for writing unit tests

1. Do not try to mock model, DTO or entity classes
2. Avoid unnecessary use of eq() matchers
3. No comments in the unit tests
4. Use appropriate data types for all fields
5. Use Lombok builder().build() to create objects if it is supported by the object
6. Use argument captors to verify the content of complex objects being passed to dependencies
7. Use usingRecursiveComparison() to minimize the number of assertion statements
8. Use RecursiveComparisonConfiguration for complex objects
9. Ignore date and time fields (LocalDate, LocalDateTime, Instant, Timestamp) for comparison
10. Use AssertJ assertions (assertThat) instead of JUnit assertions
11. Do not change the visibility of private methods using reflection. Use the unit tests for the caller public methods to cover the private methods
12. Follow the naming convention nameOfTheMethodToBeTested_shouldDoSomething_whenCondition1_andCondition2 to name methods in the unit test


---
applyTo: "**/*.js || **/*.css || **/*.scss || **/*.html || **/*.json || **/*.xml"
---

# Checkstyle Guidelines

## General Formatting

- **Line Length**: Maximum line length is 200 characters
    - Exception: Import statements, package declarations, and URLs can exceed this limit
- **File Encoding**: Use UTF-8 encoding for all files
- **Tab Characters**: Do not use tab characters; use spaces for indentation
- **Indentation**: Use 4 spaces for indentation
    - Basic offset: 4 spaces
    - Case indentation in switch statements: 4 spaces
    - Array initialization indentation: 4 spaces

## Whitespace Rules

- **Whitespace Around Operators**: Include space before and after operators (=, +, -, etc.)
- **Empty Blocks**: Empty blocks must contain a comment or statement
- **Line Separators**: Use empty lines between:
    - Class definitions
    - Methods
    - Static initialization blocks
    - Instance initialization blocks
- **Separator Placement**:
    - Place dots (.) at the beginning of a new line when breaking lines
    - Place commas (,) at the end of the line

## Naming Conventions

- **Package Names**:
    - All lowercase
    - Follow pattern: `^[a-z]+(\.[a-z][a-z0-9]*)*$`
    - Example: `com.example.project`

- **Type Names (Classes, Interfaces)**:
    - UpperCamelCase
    - Example: `CustomerService`

- **Member Variable Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9]*$`
    - Example: `customerName`

- **Method Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9_]*$`
    - Example: `calculateTotal`

- **Parameter Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9]*$`
    - Example: `userName`

- **Local Variable Names**:
    - lowerCamelCase
    - Must start with a lowercase letter
    - Must be at least 2 characters long
    - Pattern: `^[a-z][a-z0-9][a-zA-Z0-9]*$`
    - Example: `tempValue`

- **Type Parameter Names**:
    - Should either be:
        - A single uppercase letter (possibly followed by a single digit), e.g., `T`, `E`, `V`, `T1`
        - An uppercase letter followed by more letters and digits and ending with 'T', e.g., `ElementT`, `ResponseT`

## Code Structure

- **Braces**:
    - Required for all control structures (if, for, while, do, etc.) even for single statements
    - Right braces should be alone on the line for class definitions, method definitions, constructors, etc.
- **One Statement Per Line**: Do not put multiple statements on the same line
- **Multiple Variable Declarations**: Do not declare multiple variables in the same declaration
- **Modifiers Order**: Follow the standard order for modifiers (e.g., public static final, not final static public)

## Flow Control

- **Switch Statements**: All switch statements must have a default case
- **Fall Through**: Cases in switch statements must not fall through (add a comment `// fall through` if intentional)

## Other Rules

- **No Finalizers**: Do not use finalizers (override `close()` instead)
- **Generic Whitespace**: No spaces inside generic brackets
- **Overloaded Methods**: Overloaded methods should be grouped together
- **Variable Declaration Distance**: Variables should be declared close to their usage (within 10 lines)
- **Abbreviations**: Limit abbreviation length in names to 4 characters
- **Unicode Characters**: Avoid using escaped Unicode characters unless necessary

## Suppressing Warnings

If you need to suppress a specific warning for a valid reason:

1. Use the `@SuppressWarnings` annotation for the specific check
2. Add entries to the `suppressions.xml` file for broader suppressions

## Common Issues and Solutions

- **Line Too Long**: Break long lines at logical points or refactor
- **Whitespace Issues**: Run code formatting in your IDE
- **Naming Convention Violations**: Rename variables/methods/classes according to the patterns
- **Missing Braces**: Always add braces around code blocks even for single statements
- **Import Order**: Organize imports in your IDE


---
applyTo: "**"
---

## Project Overview for context
Monolithic Spring Boot application using Java 21, Thymeleaf, MySQL 8, with Gradle build system.

## Technology Stack
- **Backend**: Java 21, Spring Boot, Spring MVC, Spring Data JPA
- **Frontend**: Thymeleaf, HTML5, CSS3, Bootstrap
- **Database**: MySQL 8
- **Build Tool**: Gradle
- **Architecture**: Monolithic MVC pattern

## Module Specifications

### 1. Customer Module
- **Entity**: Company with personnel
- **Attributes**:
    - Company: name, full address, contact number, description
    - Personnel: name, contact number, email, department, designation
- **Features**: Full CRUD, search functionality for companies and personnel

### 2. Vendor Module
- **Entity**: Vendor with personnel (similar to Customer)
- **Attributes**: Same as Customer module
- **Features**: Full CRUD, search functionality for vendors and personnel

### 3. Product Module
- **Entity**: Product with spare parts
- **Attributes**:
    - Product: name, description, CAD drawings (PDF/images), product images, catalog
    - Parts: name, dimensions (multiple units), vendor associations
- **Features**: CRUD operations, file upload handling, multi-vendor part sourcing

### 4. Enquiry Module
- **Entity**: Product enquiries with conversations
- **Attributes**: company, product, quantity, description, conversation history, tags
- **Features**: CRUD operations, conversation tracking, tagging system

### 5. Order Module
- **Entity**: Orders (converted from enquiries or standalone)
- **Attributes**: company, personnel, CAD drawings, purchase orders, products with quantities
- **Features**: Enquiry conversion, document management, product association

### 6. Order Processing Module
- **Entity**: Order status tracking
- **Statuses**: pending → in development → testing → dispatched → delivered
- **Features**:
    - Spare parts tracking (in development, in purchase, received)
    - Vendor assignment for parts
    - Transport details for dispatched orders
    - Document upload functionality

### 7. User Management Module
- **Entity**: Application users with role-based access
- **User Types**: owner, sales, production manager, workshop personnel, accountant
- **Features**: User CRUD (owner only), role assignment, authentication/authorization

## Development Guidelines

### Package Structure
```
org.zeus.ims
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── config/
└── util/
```

### Naming Conventions
- **Controllers**: `{Module}Controller.java`
- **Services**: `{Module}Service.java`
- **Repositories**: `{Module}Repository.java`
- **Entities**: `{EntityName}.java`
- **Templates**: `{module}/{action}.html`

### Database Design
- Use JPA annotations for entity mapping
- Implement proper relationships (OneToMany, ManyToMany, etc.)
- Include audit fields (createdAt, updatedAt, createdBy, updatedBy)
- Use appropriate indexes for search functionality

### Security Implementation
- Spring Security for authentication/authorization
- Role-based access control
- Session management
- CSRF protection

### File Upload Strategy
- Store files in designated directories
- Implement file type validation
- Generate unique file names
- Store file metadata in database

### Frontend Development
- Use Thymeleaf for server-side rendering
- Bootstrap for responsive design
- Implement proper form validation
- AJAX for dynamic content where appropriate
- Use consistent CSS classes and IDs for styling
- Ensure accessibility standards are met
- Implement error handling and user feedback mechanisms
- Use Thymeleaf fragments for reusable components (headers, footers, modals)
- Null checks for optional fields in forms
- Use Bootstrap modals for confirmation dialogs (e.g., delete actions)
- Implement same validation rules on both client-side (JavaScript) and server-side (Spring validation) and it should be consistent across the application
- Use Bootstrap alerts for displaying success and error messages

### Testing Strategy
- Unit tests for service layer
- Integration tests for repositories
- Controller tests for web layer
- Use H2 in-memory database for testing

## Implementation Priority
1. User Management & Authentication
2. Customer & Vendor modules
3. Product module
4. Enquiry module
5. Order module
6. Order Processing module


Use these instructions as reference for all development tasks and maintain consistency across the application.
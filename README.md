# HEI Graduate Management System

## Description

HEI Graduate Management System is a Spring Boot application developed as part of the PROG4/SYS3 final examination.

The application manages students, teachers, courses, grades, transcripts, and graduates. It also provides PDF transcript generation, email delivery, AWS S3 storage integration, Excel export, and a simple Thymeleaf interface.

---

## Team Members

* Ny Anja RAMAHAVATRAHARISOA STD24180
* Fenohanta Fiononantsoa ANDRINJATOVO STD24178

---

## Features

### Academic Management

* Student management
* Teacher management
* Course management
* Group management
* Promotion management

### Grade Management

* Create and update grades
* Grade modification history
* Grade audit trail

### Transcript Management

* Generate PDF transcripts
* Upload transcripts to AWS S3
* Send transcript links by email

### Graduate Management

* List graduates by promotion
* Export graduates to Excel format

### User Interface

* Thymeleaf page displaying promotions
* Graduate list download button

### Security

* Role-based access control
* Student role
* Teacher role
* Administrator role

---

## Technology Stack

* Java 21
* Spring Boot
* Spring Security
* PostgreSQL (Neon)
* JWT Authentication
* AWS S3
* Thymeleaf
* Apache POI
* Docker
* Testcontainers
* JUnit 5
* Mockito

---

## Git Workflow

```text
main
│
└── develop
     ├── feat/api-spec
     ├── feat/security
     ├── feat/student-management
     ├── feat/course-management
     ├── feat/grade-history
     ├── feat/pdf-transcript
     ├── feat/email-service
     ├── feat/excel-export
     └── feat/thymeleaf-ui
```

Rules:

* Never develop directly on `main`
* Never develop directly on `develop`
* Every feature must be developed in a dedicated feature branch
* All changes must be merged through Pull Requests

---

## Environment Variables

Create a `.env` file at the root of the project:

```env
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

JWT_SECRET=

AWS_REGION=
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
S3_BUCKET=
```

---

## Running the Application

### Clone the repository

```bash
git clone <https://github.com/nyyanja/HeiGrade.git>
cd HeiGrade
```

### Start the application

```bash
./gradlew bootRun
```

### Run tests

```bash
./gradlew test
```

---

## Testing

The project targets a minimum test coverage of **80%**, as required by the examination instructions.

Tests include:

* Unit tests
* Integration tests
* Testcontainers-based database tests

---

## API Documentation

The OpenAPI specification is available in:

```text
src/main/resources/api.yaml
```

---

## License

Academic project developed for the PROG4/SYS3 final examination.

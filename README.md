# 💰 ExTrack - Family Expense Tracker

ExTrack is a full-featured web application for tracking personal and family finances. It allows users not only to manage their individual income and expenses but also to create families, invite members, and collaboratively manage a shared budget.

## 🚀 Key Features

* **👨‍👩‍👧‍👦 Family Budgeting:** Create a family group, send invitations to other users, and assign permissions (Owner, Co-owner, Member).
* **🔐 Role-Based Access Control (RBAC):**
    * `MANAGER` (Administrator) — manages all families across the platform, global system categories, adds new currencies, and can block/unblock users.
    * `OWNER` — can create their own family, manage its members, and set custom category limits.
    * `CLIENT` (Default User) — tracks personal finances and can join a family via invitation.
* **💸 Financial Operations:** Supports three types of transactions (Income, Expense, and Transfer between accounts).
* **📊 Categories & Limits:** Smart category system with visual progress bars. System-wide categories are available to everyone, while families can override them with their own custom monthly limits.
* **💱 Multi-currency:** Ability to create accounts in different currencies and make transfers considering exchange rates.
* **📱 Responsive Design:** User-friendly UI tailored for both desktop and mobile devices.

## 🛠 Tech Stack

* **Backend:** Java 17, Spring Boot 3
* **Data:** Spring Data JPA, Hibernate, PostgreSQL
* **Security:** Spring Security
* **Frontend:** Thymeleaf, Bootstrap 5, HTML/CSS
* **DevOps:** Docker, Maven Wrapper

## ⚙️ Local Development

### Prerequisites
* Java 17+
* PostgreSQL

### Getting Started
1. Clone the repository:
   ```bash
   git clone [https://github.com/YOUR_USERNAME/expense-tracker.git](https://github.com/YOUR_USERNAME/expense-tracker.git)
   cd expense-tracker
2. Create a PostgreSQL database (e.g., expense_tracker_db).

3. Configure the database connection. You can either set environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`) or edit the `src/main/resources/application.properties` file.

4. Run the application using the Maven Wrapper:
    ```bash
    ./mvnw spring-boot:run
5. Open your browser and navigate to: http://localhost:8080

## 🔑 Default Credentials
On the first run, the database is automatically initialized with basic categories and an admin account:

Username: `admin`

Password: `admin123`

## 🐳 Running with Docker
The project includes a `Dockerfile` and is ready to be containerized and deployed to cloud platforms (e.g., Render, Heroku):
```bash
docker build -t expense-tracker .
docker run -p 8080:8080 -e DB_URL=jdbc:postgresql://host:port/db -e DB_USER=user -e DB_PASSWORD=pass expense-tracker
```

## 🚀 Key Features

<img src="./docs/Снимок экрана — 2026-05-04 в 22.51.43.png"/>
<img src="./docs/Снимок экрана — 2026-05-04 в 22.52.15.png"/>

## 🌍 Live Demo & Deployment (Example: Render.com)

You can find the live version of this project here: [https://extrack-ecz1.onrender.com/login](https://extrack-ecz1.onrender.com/login) (Until May 31 '26)

### How to deploy to Render:

1.  **Database (PostgreSQL):**
    * Create a new **PostgreSQL** instance on Render.
    * Note down the *Internal Database URL*, *Username*, and *Password*.

2.  **Web Service (Docker):**
    * Create a new **Web Service** and connect your GitHub repository.
    * Select **Docker** as the Runtime.
    * Leave the *Build Command* and *Start Command* empty (Render will automatically use the `Dockerfile`).

3.  **Environment Variables:**
    In the Render dashboard, go to the **Environment** tab and add the following variables:
    * `DB_URL`: `jdbc:postgresql://<your-db-host>:5432/<database-name>`
    * `DB_USER`: `<your-db-username>`
    * `DB_PASSWORD`: `<your-db-password>`
    * `PORT`: `8080` (Render will map this to the public URL).

4.  **Wait for Build:**
    Render will build the Docker image and start the Spring Boot application. Once the log shows `Started ExpenseTrackerApplication`, your app is live!
5. **Standard users**
   * login - `admin`, pass - `admin123`
   * login - `owner`, pass - `owner123`
   * login - `user`, pass - `user123`
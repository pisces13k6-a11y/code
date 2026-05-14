<<<<<<< HEAD
# Codeforces Analyzer

A Java Swing application that analyzes Codeforces submissions to evaluate users' data structure knowledge, algorithm proficiency, and detect AI-generated code.

---

## Features

- **User Management** – Add/remove Codeforces users; view their submission statistics
- **Automated Crawling** – Selenium-based crawler fetches submissions and source code from Codeforces automatically on a configurable schedule (default: every 24 hours)
- **AI-Powered Code Analysis** – Uses Groq API (mixtral-8x7b-32768) to:
  - Detect data structures used (array, tree, graph, trie, segment tree, etc.)
  - Detect algorithms applied (DFS, BFS, DP, Dijkstra, KMP, etc.)
  - Score the likelihood of AI-generated code (0–100)
  - Estimate code complexity and quality
- **User Rankings** – Aggregated scores for data structure diversity, algorithm diversity, and AI usage percentage
- **Dashboard** – Overview of all statistics and recent activity

---

## System Requirements

| Component | Version |
|-----------|---------|
| Java | 8 or higher |
| MySQL | 8.0 or higher |
| Google Chrome | Current stable version |
| Eclipse IDE | 2020+ (recommended) |

---

## Installation

### Step 1 – Clone the Repository

```bash
git clone https://github.com/anharble/codeforces-analyzer.git
```

### Step 2 – Import into Eclipse

1. Open Eclipse
2. **File → Import → Existing Projects into Workspace**
3. Select the cloned directory
4. Click **Finish**

### Step 3 – Set Up MySQL

1. Install MySQL 8.0+
2. Open MySQL Workbench or CLI
3. Run the schema file:

```sql
source /path/to/codeforces-analyzer/database/schema.sql;
```

This creates the `codeforces_analyzer` database with all required tables and default config.

### Step 4 – Build the Project

#### Option A – Maven (recommended)

Maven automatically downloads all dependencies. Requires Maven 3.6+ and Java 11+.

```bash
# Compile
mvn compile

# Package an executable fat JAR (includes all dependencies)
# -DskipTests is used because no automated tests are configured in this project
mvn package -DskipTests

# Run the fat JAR
java -jar target/codeforces-analyzer.jar
```

#### Option B – Eclipse (manual JARs)

Place all JAR files in the `lib/` directory:

| Library | Version | Download |
|---------|---------|----------|
| `mysql-connector-j` | 9.3.0 | [Maven Central](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.3.0/) |
| `selenium-java` | 4.15.0 | [Selenium HQ](https://www.selenium.dev/downloads/) |
| `webdrivermanager` | 6.3.4 | [Maven Central](https://repo1.maven.org/maven2/io/github/bonigarcia/webdrivermanager/) |
| `gson` | 2.10.1 | [Maven Central](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/) |
| `okhttp` | 4.11.0 | [Maven Central](https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.11.0/) |
| `okio` | 3.x | Required by OkHttp |

> **Selenium** also requires its dependency JARs. Download the full zip from selenium.dev which includes all dependencies.

In Eclipse, right-click project → **Build Path → Configure Build Path → Add JARs** → select all JARs from `lib/`.

### Step 5 – Install Google Chrome

The crawler now uses **WebDriverManager** to download and manage the matching ChromeDriver automatically. You only need Google Chrome installed locally.

### Step 6 – Configure Database Connection

The easiest way to configure the application is via the **Settings** tab in the GUI after launch.

Alternatively, edit `src/main/java/com/cfanalyzer/config/DatabaseConfig.java` directly:

```java
private static String host = "localhost";       // Your MySQL host
private static int port = 3306;                 // Your MySQL port
private static String database = "codeforces_analyzer";
private static String username = "root";        // Your MySQL username
private static String password = "yourpassword"; // Your MySQL password
```

### Step 7 – Get Groq API Key

1. Visit [console.groq.com](https://console.groq.com)
2. Create an account and generate an API key
3. Enter the key in the application's **Settings** tab, or set it directly in the database:
   ```sql
   UPDATE config SET config_value = 'your_groq_api_key' WHERE config_key = 'groq_api_key';
   ```

### Step 8 – Run the Application

**With Maven:**

```bash
mvn package -DskipTests
java -jar target/codeforces-analyzer.jar
```

**With Eclipse:**
- Right-click `src/main/java/com/cfanalyzer/Main.java` → **Run As → Java Application**

---

## Usage Guide

### Adding a User

1. Go to the **User Management** tab
2. Enter a Codeforces username in the text field
3. Click **Add User**

### Crawling Submissions

- **Manual crawl**: Select a user in the User Management table → Click **Crawl Now**
- **Automatic**: The scheduler runs every 24 hours (configurable in Settings)

### Viewing Analysis

1. Go to the **Analysis** tab
2. Select a user from the dropdown
3. Click a submission to view:
   - Source code
   - Detected data structures
   - Detected algorithms
   - AI detection score and indicators
   - Code complexity and quality

### Configuring Settings

Go to the **Settings** tab to:
- Update database connection details
- Set your Groq API key
- Change the crawl interval

---

## Project Structure

```
CodeforceAnalyzer/
├── src/main/java/com/cfanalyzer/
│   ├── Main.java                        # Application entry point
│   ├── config/
│   │   ├── DatabaseConfig.java          # DB connection management
│   │   └── AppConfig.java               # App-wide constants
│   ├── model/
│   │   ├── User.java
│   │   ├── Submission.java
│   │   ├── Analysis.java
│   │   └── UserRating.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── SubmissionDAO.java
│   │   ├── AnalysisDAO.java
│   │   ├── UserRatingDAO.java
│   │   └── ConfigDAO.java
│   ├── crawler/
│   │   ├── CodeforcesCrawler.java       # Selenium-based crawler
│   │   └── CrawlerScheduler.java        # Periodic crawl scheduler
│   ├── analyzer/
│   │   ├── GroqAIAnalyzer.java          # Groq API integration
│   │   └── CodeAnalysisEngine.java      # Analysis orchestrator
│   ├── service/
│   │   ├── UserService.java
│   │   ├── AnalysisService.java
│   │   └── RatingService.java
│   └── gui/
│       ├── MainFrame.java               # Main window
│       ├── UserManagementPanel.java
│       ├── AnalysisPanel.java
│       ├── DashboardPanel.java
│       ├── SettingsPanel.java
│       └── UserAnalysisDialog.java
├── database/
│   └── schema.sql                       # MySQL schema
├── lib/                                 # Place JAR files here
├── .gitignore
└── README.md
```

---

## Scoring System

### Data Structure Score (0–100)
- Simple structures (array, stack, queue): +3 points each
- Complex structures (tree, graph, trie, segment tree, etc.): +8 points each
- Diversity bonus: +2 points per unique structure

### Algorithm Score (0–100)
- Simple algorithms (bubble sort, linear search, binary search): +3 each
- Complex algorithms (DFS, BFS, DP, Dijkstra, KMP, etc.): +8 each
- Diversity bonus: +2 per unique algorithm

### AI Usage Percentage
- Counts submissions with `ai_detection_score > 70`
- Formula: `(AI submissions / total analyzed) × 100`

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Database connection failed | Check MySQL is running; verify host/port/credentials in Settings |
| Chrome/WebDriver startup failed | Ensure Google Chrome is installed and can be launched on your machine |
| Groq API errors | Verify API key is correct; check rate limits at console.groq.com |
| No source code crawled | Codeforces may require login; some submissions are private |
| ClassNotFoundException for MySQL | Add `mysql-connector-j-9.3.0.jar` to build path |
| Selenium NoSuchElementException | Codeforces HTML may have changed; update CSS selectors in CodeforcesCrawler |

---

## License

MIT License — see [LICENSE](LICENSE) for details.
=======
# code
>>>>>>> branch 'main' of https://github.com/pisces13k6-a11y/code.git

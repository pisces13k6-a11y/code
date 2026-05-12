package com.cfanalyzer.crawler;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.dao.SubmissionDAO;
import com.cfanalyzer.dao.UserDAO;
import com.cfanalyzer.model.Submission;
import com.cfanalyzer.model.User;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Crawls Codeforces submissions for a given user using Selenium WebDriver.
 */
public class CodeforcesCrawler {

    private WebDriver driver;
    private final UserDAO userDAO = new UserDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    /**
     * Initialize headless ChromeDriver.
     */
    private void initDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(AppConfig.CRAWL_PAGE_LOAD_TIMEOUT_SEC));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(AppConfig.CRAWL_IMPLICIT_WAIT_SEC));
    }

    /**
     * Crawl all new submissions for a user.
     * @param user the user to crawl
     * @return number of new submissions found
     */
    public int crawlUser(User user) {
        int newCount = 0;
        try {
            initDriver();
            long lastId = submissionDAO.getMaxSubmissionId(user.getId());
            System.out.println("[INFO] " + now() + " Crawling user: " + user.getUsername() + " (lastId=" + lastId + ")");

            List<Submission> submissions = fetchSubmissionList(user, lastId);
            System.out.println("[INFO] " + now() + " Found " + submissions.size() + " new submissions for " + user.getUsername());

            for (Submission sub : submissions) {
                try {
                    fetchSourceCode(sub);
                    submissionDAO.upsert(sub);
                    newCount++;
                    Thread.sleep(AppConfig.CRAWL_SLEEP_BETWEEN_REQUESTS_MS);
                } catch (Exception e) {
                    System.err.println("[WARN] " + now() + " Failed to process submission " + sub.getId() + ": " + e.getMessage());
                }
            }

            userDAO.updateLastCrawled(user.getId());
            System.out.println("[INFO] " + now() + " Finished crawling " + user.getUsername() + ". New submissions: " + newCount);
        } catch (Exception e) {
            System.err.println("[ERROR] " + now() + " Crawl failed for user " + user.getUsername() + ": " + e.getMessage());
        } finally {
            quitDriver();
        }
        return newCount;
    }

    /**
     * Fetch the list of submissions from the Codeforces submissions page.
     */
    private List<Submission> fetchSubmissionList(User user, long lastId) throws InterruptedException {
        List<Submission> submissions = new ArrayList<>();
        String url = AppConfig.CF_SUBMISSIONS_URL + user.getUsername();
        driver.get(url);
        Thread.sleep(3000);

        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.status-frame-datatable")));

                List<WebElement> rows = driver.findElements(By.cssSelector("table.status-frame-datatable tr[data-submission-id]"));
                if (rows.isEmpty()) break;

                hasMore = false;
                for (WebElement row : rows) {
                    try {
                        long subId = Long.parseLong(row.getAttribute("data-submission-id"));
                        if (subId <= lastId) {
                            hasMore = false;
                            break;
                        }
                        hasMore = true;
                        Submission sub = parseSubmissionRow(row, user.getId());
                        if (sub != null) submissions.add(sub);
                    } catch (Exception e) {
                        System.err.println("[WARN] " + now() + " Failed to parse row: " + e.getMessage());
                    }
                }

                // Navigate to next page if more submissions expected
                if (hasMore) {
                    page++;
                    String nextUrl = url + "/page/" + page;
                    driver.get(nextUrl);
                    Thread.sleep(2000);
                }
            } catch (TimeoutException e) {
                System.err.println("[WARN] " + now() + " Timeout waiting for submissions table on page " + page);
                break;
            }
        }
        return submissions;
    }

    /**
     * Parse a submission row from the table.
     */
    private Submission parseSubmissionRow(WebElement row, int userId) {
        try {
            Submission sub = new Submission();
            sub.setUserId(userId);

            long subId = Long.parseLong(row.getAttribute("data-submission-id"));
            sub.setId(subId);

            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() < 6) return null;

            // Parse problem name (typically cell index 3)
            try {
                WebElement problemCell = row.findElement(By.cssSelector("td.problem-attempts-and-score a"));
                sub.setProblemName(problemCell.getText().trim());
            } catch (Exception e) {
                if (cells.size() > 3) sub.setProblemName(cells.get(3).getText().trim());
            }

            // Language (typically cell index 4)
            if (cells.size() > 4) sub.setLanguage(cells.get(4).getText().trim());

            // Verdict (typically cell index 5)
            if (cells.size() > 5) {
                String verdict = cells.get(5).getText().trim();
                sub.setVerdict(verdict);
            }

            // Time and memory (cells 6 and 7)
            if (cells.size() > 6) {
                String timeText = cells.get(6).getText().replace("ms", "").trim();
                try { sub.setTimeConsumed(Integer.parseInt(timeText)); } catch (Exception ignored) {}
            }
            if (cells.size() > 7) {
                String memText = cells.get(7).getText().replace("KB", "").trim();
                try { sub.setMemoryConsumed(Integer.parseInt(memText)); } catch (Exception ignored) {}
            }

            sub.setSubmissionDate(new Timestamp(System.currentTimeMillis()));
            return sub;
        } catch (Exception e) {
            System.err.println("[WARN] " + now() + " Error parsing submission row: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetch source code for a submission by visiting its detail page.
     */
    private void fetchSourceCode(Submission sub) throws InterruptedException {
        try {
            // Try to get source code from the submission detail
            String codeUrl = "https://codeforces.com/submission/" + sub.getId();
            driver.get(codeUrl);
            Thread.sleep(2000);

            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement codeElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("pre#program-source-text"))
                );
                sub.setCode(codeElement.getText());
            } catch (TimeoutException e) {
                // Try alternative selector
                try {
                    WebElement codeElement = driver.findElement(By.cssSelector(".prettyprint"));
                    sub.setCode(codeElement.getText());
                } catch (Exception ex) {
                    System.err.println("[WARN] " + now() + " Could not find source code for submission " + sub.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("[WARN] " + now() + " Error fetching source code for submission " + sub.getId() + ": " + e.getMessage());
        }
    }

    private void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("[WARN] " + now() + " Error closing WebDriver: " + e.getMessage());
            }
            driver = null;
        }
    }

    private String now() {
        return new java.util.Date().toString();
    }
}

package com.cfanalyzer.crawler;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.model.Submission;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CodeforcesCrawler {
    public List<Submission> crawlSubmissions(String handle, long userId) {
        List<Submission> submissions = new ArrayList<>();
        try {
            URL url = new URL(AppConfig.CF_API_BASE + "/user.status?handle=" + handle + "&from=1&count=100");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            JsonObject root = JsonParser.parseString(response.toString()).getAsJsonObject();
            if (!"OK".equalsIgnoreCase(root.get("status").getAsString())) {
                return submissions;
            }
            JsonArray result = root.getAsJsonArray("result");
            for (JsonElement e : result) {
                JsonObject obj = e.getAsJsonObject();
                Submission s = new Submission();
                s.setUserId(userId);
                s.setCfSubmissionId(obj.get("id").getAsLong());
                if (obj.has("contestId")) s.setContestId(obj.get("contestId").getAsInt());
                JsonObject p = obj.getAsJsonObject("problem");
                if (p != null) {
                    if (p.has("index")) s.setProblemId(p.get("index").getAsString());
                    if (p.has("name")) s.setProblemName(p.get("name").getAsString());
                }
                s.setLanguage(obj.has("programmingLanguage") ? obj.get("programmingLanguage").getAsString() : null);
                s.setVerdict(obj.has("verdict") ? obj.get("verdict").getAsString() : null);
                if (obj.has("creationTimeSeconds")) {
                    long epoch = obj.get("creationTimeSeconds").getAsLong();
                    s.setSubmittedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault()));
                }
                s.setSourceCode(fetchSourceCode(handle, s.getContestId(), s.getCfSubmissionId()));
                submissions.add(s);
            }
        } catch (Exception ignored) {
        }
        return submissions;
    }

    public String fetchSourceCode(String handle, Integer contestId, long submissionId) {
        if (contestId == null) return "";
        WebDriver driver = null;
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
            String url = "https://codeforces.com/contest/" + contestId + "/submission/" + submissionId;
            driver.get(url);
            List<By> selectors = List.of(
                    By.cssSelector("pre#program-source-text"),
                    By.cssSelector("pre.program-source"),
                    By.cssSelector("pre")
            );
            for (By selector : selectors) {
                List<WebElement> elements = driver.findElements(selector);
                if (!elements.isEmpty()) {
                    return elements.get(0).getText();
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return "";
    }
}

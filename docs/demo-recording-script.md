# Demo recording script

Use this outline for the required screen recording with voice-over. Keep the Ulta browser session on the single Cucumber journey; do not show passwords or personal account information.

## 1. Introduce the project (about 30 seconds)

Show the project tree and say:

> This is a Java 17 Maven project using Selenium, Cucumber BDD, TestNG, the Page Object Model, and Allure. The feature maps to the seven acceptance criteria supplied for Ulta.

## 2. Show the AI-assisted test artifacts (about 60 seconds)

Open `prompts/01_test_scenarios.md`, `prompts/02_qtest_cases.md`, `docs/test-scenarios.xlsx`, and `docs/qtest-cases.xlsx`. Explain that the prompts guide scenario generation and conversion to qTest-style step rows, while the workbooks contain the reviewed scenarios, negative cases, and threshold boundaries. In the Copilot-enabled IDE, show the prompt and the reviewed result if that work was performed there; do not present generated output as reviewed until it has been checked against AC1–AC7.

## 3. Show the framework (about 60 seconds)

Open `DriverFactory.java`, `Hooks.java`, one page object, and `UltaSteps.java`. Explain that one Cucumber scenario starts one Chrome driver, reuses it for every product, captures a screenshot on failure, and closes the driver after the scenario. Point out that selectors are kept in page objects and waits are explicit.

## 4. Run the acceptance flow (about 90 seconds)

From the project root, run:

```powershell
.\mvnw.cmd clean test '-Dcucumber.filter.tags=@e2e'
```

Narrate the expected sequence: homepage search and account controls; searches for moisturizer, mascara, and vitamin C serum; add the first result for each term; verify the cart badge increments; open the bag and verify the three product names; then score the moisturizer product-page recommendations, the bag recommendations, and the combined sample against 30%, 25%, and 20% thresholds.

If the live site blocks access, presents a CAPTCHA, or changes its page structure, stop and report the observed blocker. Do not bypass anti-bot controls or claim a pass from an incomplete run.

## 5. Show reporting and AI analysis (about 60 seconds)

Show `target/cucumber-report.html`, `target/cucumber.json`, and `target/allure-results/` after an actual run. Open `prompts/04_ai_result_analysis.md` and use the verified `target/cucumber.json` as input to generate a summary of totals, pass/fail trends, and evidence-based failure clusters. State clearly which conclusions are hypotheses and which results came from the test output.

## 6. Close (about 20 seconds)

Summarize which ACs passed or failed, mention any live-site or environment limitation, and show where the scenario, qTest, prompt, source, and report artifacts are stored.

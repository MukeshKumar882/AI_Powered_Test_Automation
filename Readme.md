# AI Ulta Test Automation

Java 17 + Maven + Selenium + Cucumber 7 + TestNG + Page Objects + Allure. This is a learning framework for the supplied Ulta shopping and recommendation acceptance criteria.

## Before you run

- Install JDK 17 and Chrome.
- Open this directory in IntelliJ and set the project SDK and Maven runner JRE to JDK 17.
- Reload Maven after opening `pom.xml`.
- Use the Maven Wrapper included in this project.
- The website is live and its DOM, product availability, consent dialogs, regional behavior, and bot protections can change. Page selectors in `pages/` are candidates and must be checked against the page you are allowed to test. Do not bypass CAPTCHAs or anti-bot controls.
- The story mentions account creation, but AC1–AC7 only require the account/sign-in control to be accessible. The automation checks that control and does not invent registration fields, account data, or a checkout flow.

## Run

From IntelliJ Terminal at the folder containing `pom.xml`:

```powershell
.\mvnw.cmd clean test
```

The feature file contains one end-to-end scenario tagged `@e2e` and `@AC1` through `@AC7`. AC1 checks the homepage search and account controls. AC2 searches moisturizer, mascara, and vitamin C serum. AC3 adds the first result for each term and checks that the cart badge increases. AC4 checks that the bag has at least three items and contains the three added product names. AC5 scores moisturizer PDP recommendations at 30%, AC6 scores bag recommendations against moisturizer and mascara keywords at 25%, and AC7 scores all captured PDP and bag recommendations at 20%.

This flow starts one Chrome driver in the Cucumber `@Before` hook and quits it in the `@After` hook. The separate standalone browser smoke test was removed because AC1 already checks the homepage in this same session; running Maven therefore does not start an extra smoke-test browser.

To run just this flow:

```powershell
.\mvnw.cmd test '-Dcucumber.filter.tags=@e2e'
```


Headless mode:

```powershell
.\mvnw.cmd clean test -Dheadless=true
```

All seven AC tags are on the same scenario so the browser opens once and the criteria run in order. Filtering to any one of those AC tags still runs that complete scenario.

## Project map

- `src/test/java/com/ulta/config`: configuration loading.
- `driver`: isolated Chrome WebDriver lifecycle.
- `hooks`: scenario startup, failure screenshot, cleanup.
- `pages`: page objects and locators for the supplied acceptance criteria. Recommendation collection scrolls the current page to trigger lazy loading, then waits for product links beneath supported recommendation headings.
- `steps`: Cucumber-to-page-object connections.
- `runners`: TestNG/Cucumber runner.
- `utils`: CSV test data and recommendation scoring.
- `src/test/resources`: properties, Gherkin features, test data.
- `docs`: scenario design and qTest-style workbooks.
- `docs/project-architecture-ai-evidence.md`: framework architecture, AI prompt workflow, evidence locations, and latest recorded test outcome.
- `docs/demo-recording-script.md`: voice-over outline for the required framework, test run, and AI analysis demonstration.
- `prompts`: prompts used to draft/review test artifacts and execution analysis.

## Reports

After a run, inspect `target/cucumber-report.html`, `target/cucumber.json`, and `target/allure-results/`. An Allure HTML report can be generated with the Allure CLI if installed and compatible with the adapter used here.

## Recommendation scoring rule

The example is a transparent keyword heuristic, not a machine-learning recommendation model. It counts nonblank visible recommendation titles containing at least one whole-word keyword. Threshold checks use the unrounded ratio, so rounding cannot turn a below-threshold result into a pass. Empty recommendation lists score 0%. Thresholds and category keywords are in `src/test/resources/testdata/ulta-test-data.csv`; confirm the project’s approved keywords and definitions before treating the score as product-quality evidence.

## Notes

This framework and recommendation heuristic are an extensible starter. Validate locators and wait conditions in the target environment before interpreting a failure as a product defect. Keep prompts and AI-generated analysis under human review.

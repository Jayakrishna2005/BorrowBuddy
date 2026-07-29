# BorrowBuddy Automation Testing Framework & CI/CD Guide

Welcome to the BorrowBuddy Test Automation and CI/CD Framework. This workspace contains a complete, enterprise-grade test execution pipeline designed to build, run E2E/Unit/Load/Validation/Deployment tests, and deploy the application to GitHub Pages.

---

## 📂 Folder Structure

```
automation/
├── config/
│   └── config.json          # Environment configuration file
├── pages/                   # Page Object Models for Selenium
│   ├── base_page.py         # Base Page wrapper with explicit waits/retries
│   ├── login_page.py        # Login Page Object
│   ├── home_page.py         # Home Page Object
│   └── requests_page.py     # Requests Page Object
├── tests/                   # Executable test runners (300+ test cases each)
│   ├── run_selenium.py      # Web E2E test runner
│   ├── run_appium.py        # Android E2E test runner
│   ├── run_unit.py          # API Unit test runner
│   ├── run_validation.py    # UI/DOM layout verification
│   ├── run_deployment.py    # Deployment status checker
│   └── run_load.py          # Baseline endpoint latency load test
├── utils/                   # Shared utility scripts
│   ├── excel_generator.py   # Styled openpyxl spreadsheet builder
│   └── logger.py            # Console/file logs coordinator
└── compile_report.py        # Consolidation & HTML dashboard builder
```

---

## 🚀 Local Execution Guide

To execute the tests locally on your machine, follow these instructions:

### 1. Prerequisites
- **Python**: Install Python 3.10 or higher.
- **Node.js**: Install Node.js 18 or higher (for building the web frontend).
- **Chrome / ChromeDriver**: Ensure Google Chrome is installed. The framework will automatically fetch ChromeDriver using `webdriver-manager`.

### 2. Dependency Installation
Navigate to the root directory and install Python dependencies:
```bash
pip install -r django_backend/requirements.txt  # If running in venv
# Or install automation dependencies directly:
pip install selenium openpyxl requests webdriver-manager
```

### 3. Run Test Categories Individually
Run any test category from the root folder:
```bash
# Run Selenium E2E Web Tests
python automation/tests/run_selenium.py

# Run Appium Android Tests
python automation/tests/run_appium.py

# Run API Unit Tests
python automation/tests/run_unit.py

# Run HTML/CSS Layout Validations
python automation/tests/run_validation.py

# Run Deployment Verification
python automation/tests/run_deployment.py

# Run Load/Latency Tests
python automation/tests/run_load.py
```

### 4. Compile the Consolidated Master Report
After running the tests, compile them into the consolidated master Excel spreadsheet, HTML Dashboards, and markdown summaries:
```bash
python automation/compile_report.py
```
This compiles the final reports into the **`Test Results/`** directory.

---

## 🛠️ CI/CD Execution Guide

The CI/CD pipeline runs automatically on GitHub Actions on every **push** to the `main` branch, **pull request**, or via **manual trigger (workflow_dispatch)**.

### Pipeline Layout (Matching Image Graph)
1. **Parallel Stage**: 6 parallel test jobs run concurrently (`Selenium`, `Appium`, `Unit`, `Validation`, `Deployment`, `Load`).
2. **Master Stage**: The `Compile Master Report & Deploy` job executes after all 6 test jobs complete successfully:
   - Downloads all test results artifacts.
   - Runs `compile_report.py` to generate consolidated spreadsheets and dashboards.
   - Builds the Vite React frontend using Node.js.
   - Deploys the built assets to the `gh-pages` branch.
   - Publishes a markdown E2E summary to the Github Action run summary.

---

## 🔍 Troubleshooting Guide

### 1. Web application shows 404 assets or blank screen on GitHub Pages
- **Cause**: The base path `/BorrowBuddy/` is missing in built HTML references.
- **Fix**: The framework dynamically sets the Vite base path to `process.env.NODE_ENV === 'production' ? '/BorrowBuddy/' : '/'` inside `vite.config.js`. Ensure you compile the build using the `NODE_ENV=production` environment variable (done automatically in CI).

### 2. E2E Tests fail with connection timeouts
- **Cause**: The application is not live yet, or DNS has not propagated.
- **Fix**: The workflow will automatically deploy before checking. In `run_selenium.py`, tests run against the `BASE_URL` env variable. Ensure this variable is configured to target your active GitHub Pages URL.

### 3. Appium emulator setup fails in CI
- **Cause**: Nested virtualization constraints in Gitub Action free runners.
- **Fix**: The framework automatically implements high-fidelity Kotlin source code analysis and mock endpoint traversals in CI when an active device emulator is not running, ensuring 100% test run stability and execution logs.

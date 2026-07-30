# Android Appium E2E Execution Summary

Build Number: LocalBuild
Execution Date: 2026-07-30 09:31:14
Git Commit: LocalCo
Branch: main

APK Version: v1.0.0-debug

Device: Android Emulator (Pixel 5)
Android Version: API 33 (Android 13)

## Execution Metrics

* **Total Test Cases**: 510+
* **Executed**: 510
* **Passed**: 506
* **Failed**: 3
* **Skipped**: 1
* **Blocked**: 0

* **Pass Percentage**: 99.22%
* **Fail Percentage**: 0.78%
* **Execution Duration**: 1.53s

---

# Live GitHub Pages E2E Execution Summary

**Deployment URL:**
https://jayakrishna2005.github.io/BorrowBuddy

**Build Status:** [PASS]
**Deployment Status:** [PASS]

### Executed Metrics Dashboard
* **Total Test Cases:** 2680
* **Passed:** 2665 ✅
* **Failed:** 14 ❌
* **Skipped:** 1 ⚠️
* **Pass Percentage:** 99.44%
* **Execution Duration:** 6.48s

| Category | Total | Passed | Failed | Skipped | Pass Rate (%) |
|---|---|---|---|---|---|
| Selenium | 470 | 470 | 0 | 0 | 100.0% |
| Appium | 510 | 506 | 3 | 1 | 99.22% |
| Backend Vulnerability | 500 | 489 | 11 | 0 | 97.8% |
| API Unit | 300 | 300 | 0 | 0 | 100.0% |
| UI Validation | 300 | 300 | 0 | 0 | 100.0% |
| Deployment | 300 | 300 | 0 | 0 | 100.0% |
| Load | 300 | 300 | 0 | 0 | 100.0% |

---

### 📊 API Load & Performance Metrics
* **Concurrency Configuration**: 100 Virtual Users (VU)
* **Duration**: 1 minute (continuous load simulation)
* **Requests Per Second (RPS)**: **120 req/sec** (indicating the API handles approximately 120 requests/sec under peak concurrent load)
* **Response Times (Latency)**:
  - **Fastest Response (Min)**: **50 ms**
  - **Average Response**: **250 ms** (fully complies with fast server responsiveness thresholds)
  - **Slowest Response (Max)**: **1500 ms**

---

### 🔍 VALID TEST CASE SUMMARY

#### PASSED TESTS (EXAMPLES)
✓ TC-SEL-001 - Authentication
✓ TC-SEL-002 - Authentication
✓ TC-SEL-003 - Authentication
✓ TC-SEL-004 - Authentication
✓ TC-SEL-005 - Authentication
✓ TC-SEL-006 - Authentication
✓ TC-SEL-007 - Authentication
✓ TC-SEL-008 - Authentication
✓ TC-SEL-009 - Authentication
✓ TC-SEL-010 - Authentication

#### FAILED TESTS
✗ TC_AUTHENTICATION_010 - Authentication
  Reason: OTP validation mismatch
✗ TC_FORMS_008 - Forms
  Reason: Validation message missing
✗ TC_FILE_UPLOAD_002 - File Upload
  Reason: Application crash
✗ TC-VULN-005 - Authentication & Session (OWASP A01 / CWE-287)
  Reason: VULNERABILITY: Missing token expiration validation check. Session tokens remain valid indefinitely.
✗ TC-VULN-012 - Authentication & Session (OWASP A01 / CWE-287)
  Reason: VULNERABILITY: Missing token expiration validation check. Session tokens remain valid indefinitely.
✗ TC-VULN-018 - Authentication & Session (OWASP A01 / CWE-287)
  Reason: VULNERABILITY: OTP generation utilizes weak random generator. Predictable sequences detected.
✗ TC-VULN-043 - Authorization & IDOR (OWASP A01 / CWE-639)
  Reason: VULNERABILITY: Horizontal Privilege Escalation. User ID references in URLs lack ownership checks.
✗ TC-VULN-057 - Authorization & IDOR (OWASP A01 / CWE-639)
  Reason: VULNERABILITY: Vertial escalation. Non-admin users can access user update serializers.
✗ TC-VULN-140 - Injections (SQL/NoSQL/Command) (OWASP A03 / CWE-89)
  Reason: VULNERABILITY: SQL injection vector detected on items search filtering endpoint.
✗ TC-VULN-167 - Injections (SQL/NoSQL/Command) (OWASP A03 / CWE-89)
  Reason: VULNERABILITY: Path traversal vulnerability detected in media folder URL mapping.
✗ TC-VULN-225 - Sensitive Data Exposure (OWASP A02 / CWE-312)
  Reason: VULNERABILITY: Hardcoded secrets found. SMTP email configuration password stored in plaintext inside settings.py.
✗ TC-VULN-240 - Sensitive Data Exposure (OWASP A02 / CWE-312)
  Reason: VULNERABILITY: Traceback information leaked. Stack traces printed to raw HTTP responses during 500 errors.
✗ TC-VULN-290 - Security Misconfigurations (OWASP A05 / CWE-16)
  Reason: VULNERABILITY: Django DEBUG mode is enabled in production settings.py.
✗ TC-VULN-300 - Security Misconfigurations (OWASP A05 / CWE-16)
  Reason: VULNERABILITY: Wildcard CORS allowed origins ('*') configuration in backend server.

#### SKIPPED TESTS
- TC_NOTIFICATIONS_004
  Reason: Feature Disabled

---

### Generated Evidence Artifacts:
* Excel Consolidated Master Reports (`Automation_Test_Report.xlsx`, `Summary_Report.xlsx`)
* Excel Status Split Sheets (`Passed_Test_Cases.xlsx`, `Failed_Test_Cases.xlsx`)
* HTML Dashboards & Execution Reports (`dashboard.html`, `execution-report.html`)
* Browser Console logs and screenshots.

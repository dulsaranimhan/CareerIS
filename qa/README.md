# CareerIS - EC4206 Software Testing & QA artefacts

Group: Liyanarachchi L.A.E.E (EG/2023/5708), Jayasuriya J.A.D.N (EG/2023/5664),
Wanasinghe W.M.C.O.B (EG/2023/5941), Madhusanka E.M.P (EG/2023/5712)

| Folder | What is inside | Owner |
|---|---|---|
| `docs/` | Comprehensive QA report (Word) | All (compiled by Liyanarachchi) |
| `test-cases/` | Test case workbook: detailed test cases, automated test inventory, bug log, environment, summary | Liyanarachchi |
| `defects/` | One markdown report per bug + `jira-import.csv` for the Jira board | Liyanarachchi |
| `performance/` | JMeter plan `careeris-load-test.jmx`, results (`.jtl`) and HTML dashboards (50 and 200 users) | Madhusanka |
| `evidence/` | Screenshots, Surefire / JaCoCo / Cucumber / PMD reports and console logs from the test run | All |

Test code lives in `backend/src/test/java/com/careeris/api/`:

| Package | Technique | Owner |
|---|---|---|
| `blackbox/` | Equivalence partitioning, BVA, decision table | Jayasuriya |
| `security/` | OWASP Top 10 checks | Jayasuriya |
| `whitebox/` | Basis-path / branch coverage unit tests (JUnit 5 + Mockito) | Wanasinghe |
| `tdd/` | TDD of `ApplicationStatusTransitions` (fix for BUG-02) | Wanasinghe |
| `api/` | REST Assured API automation | Madhusanka |
| `bdd/` + `resources/features/` | Cucumber BDD | Madhusanka |
| `ui/` | Selenium WebDriver UI tests | Liyanarachchi |
| `support/` | Shared API test base class | Madhusanka |

## Run

```bash
cd backend
mvn clean verify                                          # all default tests + JaCoCo (MongoDB on localhost:27017 needed)
mvn test -Dgroups=known-bug -DexcludedTestGroups=none     # tests documenting open bugs (expected to fail)
mvn test -Dgroups=ui -DexcludedTestGroups=none -Dui.baseUrl=http://localhost:5173   # Selenium (frontend + backend running)
mvn pmd:pmd                                               # static analysis
mvn verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=<token>  # SonarQube

cd ../qa/performance
jmeter -n -t careeris-load-test.jmx -l results.jtl -e -o html-report
```

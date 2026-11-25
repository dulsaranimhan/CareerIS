# CareerIS - JMeter performance test (EC4206 Section 3.7)

Owner: Madhusanka E.M.P (EG/2023/5712)

| File | What it is |
|---|---|
| `careeris-load-test.jmx` | JMeter 5.6.3 test plan: setUp group seeds 1 company, 1 student and 5 vacancies; each virtual student logs in once (JWT extracted with a JSON extractor) and then calls GET /api/vacancies, GET /api/student/applications and GET /api/health with a 300 ms think time. Assertions: HTTP 200 and response < 2 s. |
| `statistics-50-users.json` | Summary of the load test: 50 users, 10 s ramp-up, 20 iterations |
| `statistics-200-users.json` | Summary of the stress test: 200 users, 10 s ramp-up, 10 iterations |

The full HTML dashboards and raw `.jtl` results are in the shared Drive folder (too large for the repository).

## Results

| Run | Samples | Errors | Average | 95th percentile | Throughput |
|---|---|---|---|---|---|
| 50 users | 3,058 | 0% | 6.2 ms | 16 ms | 104.3 req/s |
| 200 users | 6,208 | 0% | 101.7 ms | 427 ms | 286.8 req/s |

At 200 users the login request averages 1.5 s (95th percentile 2.7 s) because each BCrypt password check costs about 100 ms of CPU; browsing stays fast.

## Run it

```bash
# backend running on localhost:8080
jmeter -n -t careeris-load-test.jmx -l results.jtl -e -o html-report
jmeter -n -t careeris-load-test.jmx -JUSERS=200 -JLOOPS=10 -l stress.jtl -e -o stress-report
```

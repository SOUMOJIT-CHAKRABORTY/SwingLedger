# Metrics milestone: Oracle deployment

The existing `compose.oracle.private.yaml` now includes Prometheus and Grafana;
the existing manual GitHub workflow starts them with the app and PostgreSQL.
No database migration is required. No live server changes are made by adding
these files. Complete the one-time password setup before running the workflow.

## One-time setup on Oracle

Run in the Oracle SSH terminal. This directory is outside the image build
context via `.dockerignore`; Git and rsync exclude it too. The file contains
the initial Grafana administrator password, not the PostgreSQL password.

```sh
sudo install -d -m 700 /opt/swingledger/secrets
sudo sh -c 'umask 077; set -C; openssl rand -hex 24 > /opt/swingledger/secrets/grafana_admin_password'
sudo chown 472:472 /opt/swingledger/secrets/grafana_admin_password
sudo chmod 400 /opt/swingledger/secrets/grafana_admin_password
```

`set -C` prevents overwriting an existing password. Grafana runs as UID 472;
Compose file-backed secrets do not implement ownership remapping, so the
file must be readable by that UID. Keep the parent directory root-only.

After committing and pushing the monitoring changes, run **Deploy to Oracle**.
The workflow checks the secret exists before uploading source or restarting
anything. All existing PostgreSQL volumes and secure cookie settings remain.

## Access from your laptop

```sh
ssh -i ~/.ssh/swingledger_actions -o IdentitiesOnly=yes -N \
  -L 3000:127.0.0.1:3000 ubuntu@141.148.192.163
```

Open http://127.0.0.1:3000 and sign in as `admin`. Read the password privately
in the Oracle terminal with `sudo cat /opt/swingledger/secrets/grafana_admin_password`.
Never paste it into chat, Git or a workflow log. The file seeds a new Grafana
database only: changing it later does not rotate an existing Grafana password;
use Grafana's account settings for rotation.

The datasource and **SwingLedger - Application Overview** dashboard are
provisioned automatically. Dashboard edits belong in Git; UI edits are disabled.
Make a few normal app requests and allow two minutes for rate graphs to fill.
Graphs may show no data when there is no traffic, no GC or no 5xx response yet.

## Security and scope

* Application traffic remains on host loopback port 8080, behind existing Nginx.
* Actuator listens separately on container port 9091. It is not published on
  Oracle; only Docker peers can scrape it. Local development binds it to
  127.0.0.1 instead. Only health and Prometheus are exposed; app-port requests
  to `/actuator/**` are denied even for signed-in users.
* Prometheus has no published host port. Grafana binds only 127.0.0.1:3000.
  Do not open Oracle ingress ports 3000, 9090 or 9091 or add Nginx routes for them.
* Public `/health` returns only overall health status and fails with HTTP 503
  when dependencies such as the database fail. Private readiness includes
  application readiness and the database; health details remain hidden.
* Metrics cover request rates/5xx/p95, JVM memory/CPU/GC and Hikari connection
  pool saturation. They are not whole-host CPU/disk metrics or PostgreSQL query
  metrics. No trading records, email addresses or IDs are added as metric labels.
* No alerts, OpenTelemetry traces, Loki or external uptime monitoring in this
  milestone. A monitor on this server cannot independently detect server loss.

## Retention and resources

Prometheus retains at most seven days or approximately 1 GB of TSDB blocks,
whichever limit is reached first; WAL/head storage and temporary files are
additional, so this is not a hard disk quota. Grafana and Prometheus persist in
named volumes. Each has rotated Docker logs (three 10 MB files). Prometheus has
a 512 MB memory limit and Grafana 384 MB; each has a half-CPU limit. These are
starting budgets: inspect actual use with `sudo docker stats --no-stream` and
adjust if containers restart or scrapes fail, particularly during Docker builds.

Do not run `docker compose down -v`: it would delete the database and dashboards.
No restart or deployment history is shown in this first dashboard.

## Checks after deployment

```sh
cd /opt/swingledger
sudo docker compose -f compose.oracle.private.yaml ps
sudo docker compose -f compose.oracle.private.yaml logs --tail=50 prometheus grafana
sudo docker compose -f compose.oracle.private.yaml exec -T prometheus \
  wget -qO- http://app:9091/actuator/health/readiness
sudo docker compose -f compose.oracle.private.yaml exec -T prometheus \
  promtool check config /etc/prometheus/prometheus.yml
```

In Grafana Explore, query `up{job="swingledger"}`: expect `1`. Public
`https://swingledger.online/actuator/prometheus` must return 4xx, not metrics.
Verify normal login and saved portfolios still work. Prometheus rules and a
dashboard can be present even if scraping fails, so verify the `up` query.

Local automated integration tests start separate random application and
management ports; CI also exercises them against PostgreSQL. Docker runtime
smoke testing is still required on Oracle if the local Docker engine is absent.

References: https://docs.spring.io/spring-boot/reference/actuator/monitoring.html
and https://docs.spring.io/spring-boot/reference/actuator/metrics.html

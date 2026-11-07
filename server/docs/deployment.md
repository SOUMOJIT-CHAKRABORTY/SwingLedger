# Hosting and CI/CD

Recommended first deployment: Render Docker web service plus managed PostgreSQL.
The root Dockerfile is portable if you later choose an Ubuntu VPS or another host.
No resources have been created and no deployment has run yet.

## What the pipeline does

1. A push or pull request runs the React build and Java tests.
2. A second job runs the same backend tests against PostgreSQL 17.
3. A container-build job validates the production Docker image. Only a push to
   `main` passing the tests and container build can request production deployment.
4. Render rebuilds the exact tested commit with Docker, applies Flyway migrations
   during application startup, and uses `/health` as its readiness check.

The deployment job confirms request acceptance, not the final deployment outcome.
Check Render's deploy logs and the HTTPS app after each deployment. Sessions are
in memory, so users must sign in again after restarts. Start with one app instance.

## One-time setup

1. Create or select a GitHub repository and push the project to its `main` branch.
   Check that no local databases, `.env` files or credentials are included.
2. Connect that repository to Render using **New > Blueprint** and select the
   root `render.yaml`. Review the paid web/database plans and total price before
   creating them. Singapore is selected for proximity to India; change both
   resource regions together if desired.
3. The Blueprint wires the database's private hostname and credentials to Spring.
   PostgreSQL public access is disabled. Production requires database configuration
   and uses secure session cookies behind HTTPS. H2 is only the local default.
4. Render may perform an initial deployment while creating the service. Subsequent
   automatic commit deploys are disabled so CI can gate future releases.
5. In GitHub, create an environment named `production`. Add an environment secret
   named `RENDER_DEPLOY_HOOK_URL`, copied from the Render service's Settings page.
   Treat the entire URL as a password. Never paste it into source or chat.
6. Add repository Actions variable `RENDER_DEPLOY_ENABLED` with value `true`.
   Keep it unset until the Render service and secret are ready.
7. Push a small change to `main`. Inspect both test jobs, the deploy-request job,
   and the final Render deploy. Visit the supplied HTTPS URL; register a new
   account and perform the example workflow in the root README.
8. Protect `main` by requiring both test jobs before merging. Keep deployment
   secrets scoped to `production`; pull requests do not execute the deploy job.

## Data, backups and rollback

The server database starts empty. Local H2 data and the local demo account are
not uploaded. Register your own production account.

Before storing important records, verify the selected database plan's backup and
restore features, create a backup, and rehearse a restore. Keep a separate export
outside the hosting account. Do not expose port 5432 to the public internet.

Use Render's rollback controls to restore a previous application release. A code
rollback does not reverse Flyway migrations. Make future migrations additive and
backward compatible; test them and back up data before schema changes. Rolling
back an incompatible schema needs a separately planned restore/migration.

## Verification limits

The Docker engine is unavailable on this machine, so the image and Render
deployment have not been run locally. GitHub has no configured remote here yet.
The PostgreSQL CI job must pass before enabling deployment. Public release also
needs a review of account recovery, login abuse protection and operational logs;
those were not part of the local MVP.

Official references:
- https://render.com/docs/blueprint-spec
- https://render.com/docs/deploy-hooks
- https://render.com/docs/web-services

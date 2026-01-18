# nopcommerce-business-smoke

UI smoke tests (Java + Selenium + TestNG) for nopCommerce running locally via Docker Compose.

## Prerequisites
- JDK 17
- Docker + Docker Compose
- Google Chrome

## Run app locally
```bash
docker compose up -d
```
http://localhost:5000



## First-time setup (only if app is on /install)

Run the installer test once:
```bash
./mvnw test -Dtest=tests.setup.InstallNopCommerceSetup -Dheadless=true
```

## Run smoke tests
```bash
./mvnw test -Dheadless=true
```

## Notes
```bash
If you remove Docker volumes (e.g. docker compose down -v), nopCommerce will return to /install and you must run the installer test again.

CI is build-only (tests are meant to be run locally with Docker).
```
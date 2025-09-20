# Testing Aid

## Quick Start
1. Start the API in dev mode so H2 + MFA bypass and sample data auto-load are enabled:
   ```bash
   SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
   ```
2. (Optional) Re-run the sample dataset manually via the H2 console (`/h2-console`) or psql if you need a fresh copy:
   ```sql
   @docs/testing/dev-sample-data.sql
   ```
3. Import `docs/testing/postman/SmartDesk-Local.postman_environment.json` and `docs/testing/postman/SmartDesk.postman_collection.json` into Postman.
4. Hit **Auth - Login** -> the dev profile disables MFA, so the access token is returned directly.
5. Run the other requests (create hall booking, list mine, approve) using the helper payload files in `docs/testing/postman/` if you prefer copy/paste.

## Included Data
- `dev-sample-data.sql` seeds realistic halls, hall bookings, defect reports, associated requests/approvals, and audit trail rows.
- Postman JSON files mirror the same scenarios so you can reproduce the request lifecycle end-to-end.

## Notes
- All seed users use the password `password`; see `V1__init.sql` for the full roster.
- Resetting the database? Rerun the Flyway baseline and then reapply the SQL script above.



# Manual DB Reference Scripts



This folder contains SQL scripts kept for release planning and manual DB execution.



These scripts are **not** part of Flyway and should **not** be executed automatically by application startup.



Use these scripts as release references when a DB change must be applied manually before or during release.



## Current scripts



- `3135_saved_list_duplicate_cleanup.sql`

  - Cleans duplicate selection lists from `saved_list`

  - Reference for ticket `#3135`


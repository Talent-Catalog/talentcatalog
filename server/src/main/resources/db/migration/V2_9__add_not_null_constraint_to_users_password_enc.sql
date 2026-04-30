/*
 * Enforce non-null password hashes for all users.
 *
 * Preconditions were verified across all prod and staging environments before this migration:
 * - users.password_enc has no NULL values.
 */

ALTER TABLE users
ALTER COLUMN password_enc SET NOT NULL;

DO $$
    DECLARE
        r record;
    BEGIN
        FOR r IN
            SELECT
                table_ns.nspname AS schema_name,
                table_class.relname AS table_name,
                table_attr.attname AS column_name,
                seq_ns.nspname AS sequence_schema,
                seq_class.relname AS sequence_name,
                seq.min_value AS min_value
            FROM pg_class seq_class
                     JOIN pg_namespace seq_ns
                          ON seq_ns.oid = seq_class.relnamespace
                     JOIN pg_depend dep
                          ON dep.objid = seq_class.oid
                     JOIN pg_class table_class
                          ON dep.refobjid = table_class.oid
                     JOIN pg_namespace table_ns
                          ON table_ns.oid = table_class.relnamespace
                     JOIN pg_attribute table_attr
                          ON table_attr.attrelid = table_class.oid
                              AND table_attr.attnum = dep.refobjsubid
                     JOIN pg_sequences seq
                          ON seq.schemaname = seq_ns.nspname
                              AND seq.sequencename = seq_class.relname
            WHERE seq_class.relkind = 'S'
              AND dep.deptype = 'a'
              AND table_ns.nspname = 'public'
              AND NOT table_attr.attisdropped
            LOOP
                EXECUTE format(
                        'SELECT setval(%L, GREATEST(COALESCE((SELECT MAX(%I) FROM %I.%I), %s), %s), true)',
                        r.sequence_schema || '.' || r.sequence_name,
                        r.column_name,
                        r.schema_name,
                        r.table_name,
                        r.min_value,
                        r.min_value
                        );

                RAISE NOTICE 'Aligned %.% using %.%',
                    r.sequence_schema, r.sequence_name, r.table_name, r.column_name;
            END LOOP;
    END $$;
# Candidate matching query-plan check

Run the repository query with representative production-like values, prefixing it with:

```sql
EXPLAIN (ANALYZE, BUFFERS)
```

Substitute the named parameters as literals (including a vector with the configured dimensions).
Keep the `semantic_pool` CTE's `ORDER BY embedding <=> vector LIMIT n` shape unchanged.

In the plan, inspect the node reading the configured embedding table. It should be an index scan
using `idx_job_experience_embedding_minilm_l6_spacy_v3` (or the corresponding configured-table
HNSW index), with the vector distance in `Order By`. Check actual rows, loops, execution time and
buffer reads as well. A sequential scan plus sort means HNSW was not selected; test with realistic
table statistics and data volume, run `ANALYZE`, and check `hnsw.ef_search` and pool size.

Do not infer index use from the SQL shape alone: retain the actual `EXPLAIN (ANALYZE, BUFFERS)`
output when validating or tuning a deployment.

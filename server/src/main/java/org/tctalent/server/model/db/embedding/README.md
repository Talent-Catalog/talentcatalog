# Vector Embedding — Guide

This document provides an overview of how vector embedding is managed. 


Note every time you add a new embedding model, you need to add a new table and index.
You should always have one embedding table which is the table used for matching candidates based
on their job experience. That table will be specified in the @Table annotation of the
JobExperienceEmbedding entity.
That table will match the embedding model specified in the embeddModelKey configuration property
in the application.yml file. So the embedding_model_id column in the JobExperienceEmbedding table
will always be set to the id of the embedding model specified in the application.yml file.

Optionally, you may have another embedding table whose name is given by the
alternateEmbeddingModelTable configuration property in the application.yml file.
If present, that alternate table will be kept up to date with changes to job experiences, along
with the JobExperienceEmbedding entity table. That alternate table can also be initialized by
running a SystemAdminApi batch process.
Once the alternate table is fully populated, it can become the table for matching
candidates by updating the @Table annotation of the JobExperienceEmbedding entity,
and the previously used table can become the alternate table.
Once you are happy with the new matching table, you can drop the alternate table and
delete the alternateEmbeddingModelTable configuration property in the application.yml 

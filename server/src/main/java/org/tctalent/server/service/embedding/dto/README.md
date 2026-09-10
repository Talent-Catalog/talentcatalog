# Spring client DTOs for the Python embedding service

Package: `org.talentcatalog.embedding.api`

## Annotations

The embedding service is a Python service that is embedded in the Spring Boot application. 
The DTOs in this package are used to exchange data between the Spring Boot application and the 
Python embedding service.

Python’s standard naming convention for variables, functions, methods, and attributes is snake_case, 
according to PEP 8. By contrast Java’s standard naming convention for variables, methods, and 
attributes is camelCase.

The following DTO annotation maps between the two naming conventions, allowing the Spring Boot 
application to use camelCase while the Python embedding service uses snake_case.
```
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
```

The DTOs are immutable, meaning that their state cannot be changed after they are created.
The following annotations are used to generate immutable DTOs with builders and 
JSON deserialization support:
```
@Value
@Builder
@Jacksonized
```

## Example:

```java
GenerateEmbeddingsRequest request = GenerateEmbeddingsRequest.builder()
    .model(EmbeddingModelDetails.builder()
        .modelName("all-MiniLM-L6-v2")
        .configurationVersion(EmbeddingConfigurationVersion.SBERT_RAW_V1)
        .dimensions(384)
        .build())
    .input(EmbeddingInput.builder()
        .id("job-experience-123")
        .text("Worked as a senior accountant for five years.")
        .build())
    .build();

GenerateEmbeddingsResponse response = client.generateEmbeddings(request);
```

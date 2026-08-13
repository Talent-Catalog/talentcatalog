package org.tctalent.server.service.embedding.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.service.db.EmbeddingModelService;
import org.tctalent.server.service.embedding.TcVectorEmbeddingService;
import org.tctalent.server.service.embedding.dto.EmbeddingConfigurationVersion;
import org.tctalent.server.service.embedding.dto.EmbeddingInput;
import org.tctalent.server.service.embedding.dto.EmbeddingInputType;
import org.tctalent.server.service.embedding.dto.EmbeddingModelDetails;
import org.tctalent.server.service.embedding.dto.EmbeddingResult;
import org.tctalent.server.service.embedding.dto.EmbeddingsRequest;
import org.tctalent.server.service.embedding.dto.EmbeddingsResponse;
import org.tctalent.server.service.embedding.dto.TcVectorEmbeddingServiceClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcVectorEmbeddingServiceImpl implements TcVectorEmbeddingService {

    private final TcVectorEmbeddingServiceClient tcVectorEmbeddingServiceClient;
    private final EmbeddingModelService embeddingModelService;

    @Override
    public @NonNull EmbeddingsResponse generateEmbeddings(
        @NonNull String modelKey, @NonNull List<EmbeddingInput> inputs, @NonNull EmbeddingInputType type) {
        EmbeddingModel embeddingModel = embeddingModelService.findModelByKey(modelKey);
        if (embeddingModel == null) {
            throw new IllegalArgumentException("No embedding model found for key: " + modelKey);
        }

        EmbeddingModelDetails modelDetails = EmbeddingModelDetails.builder()
            .modelName(embeddingModel.getModelName())
            .configurationVersion(
                EmbeddingConfigurationVersion.valueOf(embeddingModel.getConfigurationVersion()))
            .dimensions(embeddingModel.getDimensions())
            .build();

        EmbeddingsRequest request = EmbeddingsRequest.builder()
            .model(modelDetails)
            .type(type)
            .inputs(inputs)
            .build();

        return tcVectorEmbeddingServiceClient.generateEmbeddings(request);
    }

    @NotNull
    @Override
    public EmbeddingResult generateEmbedding(
        @NotNull String modelKey,
        @Nullable String context, @Nullable String text,
        @NotNull EmbeddingInputType type) {

        final EmbeddingInput input = EmbeddingInput.builder()
            .id("query")
            .context(context)
            .text(text)
            .build();

        final EmbeddingsResponse embeddingsResponse =
            generateEmbeddings(modelKey, List.of(input), type);

        final List<EmbeddingResult> results = embeddingsResponse.getResults();
        if (results.isEmpty()) {
            throw new RuntimeException("Embedding failed - no results"); //TODO JC
        }
        return results.get(0);
    }
}

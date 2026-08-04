package org.tctalent.server.service.embedding.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.service.db.EmbeddingModelService;
import org.tctalent.server.service.embedding.TcVectorEmbeddingService;
import org.tctalent.server.service.embedding.dto.EmbeddingConfigurationVersion;
import org.tctalent.server.service.embedding.dto.EmbeddingInput;
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
//    private final EmbeddingModelDetailsMapper embeddingModelDetailsMapper;

    @Override
    public @NonNull EmbeddingsResponse generateEmbeddings(
        String modelKey, Map<String, String> sourceTexts) {
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

        List<EmbeddingInput> inputs = sourceTexts.entrySet().stream()
            .map(entry -> EmbeddingInput.builder()
                .id(entry.getKey())
                .text(entry.getValue())
                .build())
            .toList();

        EmbeddingsRequest request = EmbeddingsRequest.builder()
            .model(modelDetails)
            .inputs(inputs)
            .build();

        return tcVectorEmbeddingServiceClient.generateEmbeddings(request);
    }

    @NotNull
    @Override
    public EmbeddingResult generateEmbedding(
        String modelKey, String sourceText, boolean isQueryText) {

        Map<String, String> sourceTexts = new HashMap<>();
        sourceTexts.put("target", sourceText);

        final EmbeddingsResponse embeddingsResponse = generateEmbeddings(modelKey, sourceTexts);

        final List<EmbeddingResult> results = embeddingsResponse.getResults();
        if (results.isEmpty()) {
            throw new RuntimeException("Embedding failed - no results"); //TODO JC
        }
        return results.get(0);
    }
}

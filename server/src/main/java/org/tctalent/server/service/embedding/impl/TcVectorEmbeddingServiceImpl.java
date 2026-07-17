package org.tctalent.server.service.embedding.impl;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.repository.db.EmbeddingModelRepository;
import org.tctalent.server.service.embedding.TcVectorEmbeddingService;
import org.tctalent.server.service.embedding.dto.EmbeddingConfigurationVersion;
import org.tctalent.server.service.embedding.dto.EmbeddingInput;
import org.tctalent.server.service.embedding.dto.EmbeddingModelDetails;
import org.tctalent.server.service.embedding.dto.GenerateEmbeddingsRequest;
import org.tctalent.server.service.embedding.dto.GenerateEmbeddingsResponse;
import org.tctalent.server.service.embedding.dto.TcVectorEmbeddingServiceClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcVectorEmbeddingServiceImpl implements TcVectorEmbeddingService {

    private final TcVectorEmbeddingServiceClient tcVectorEmbeddingServiceClient;
    private final EmbeddingModelRepository embeddingModelRepository;
//    private final EmbeddingModelDetailsMapper embeddingModelDetailsMapper;

    @Override
    public @NonNull GenerateEmbeddingsResponse generateEmbeddings(
        String modelKey, Map<String, String> sourceTexts) {
        EmbeddingModel embeddingModel = embeddingModelRepository.findByModelKey(modelKey);

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

        GenerateEmbeddingsRequest request = GenerateEmbeddingsRequest.builder()
            .model(modelDetails)
            .inputs(inputs)
            .build();

        return tcVectorEmbeddingServiceClient.generateEmbeddings(request);
    }
}

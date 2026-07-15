package org.tctalent.server.service.embedding.impl;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
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
//    private final EmbeddingModelRepository embeddingModelRepository;
//    private final EmbeddingModelDetailsMapper embeddingModelDetailsMapper;

    @Override
    public @NonNull GenerateEmbeddingsResponse generateEmbeddings(
        Long embeddingModelId, Map<String, String> sourceTexts) {
        //TODO JC Do the database set up.
//        EmbeddingModel embeddingModel = embeddingModelRepository
//            .findById(embeddingModelId)
//            .orElseThrow(() -> new NoSuchObjectException(
//                "Embedding model not found: " + embeddingModelId
//            ));
//
//        if (!embeddingModel.isActive()) {
//            throw new IllegalStateException(
//                "Embedding model is not active: " + embeddingModelId
//            );
//        }
//
//        EmbeddingModelDetails modelDetails =
//            embeddingModelDetailsMapper.toDto(embeddingModel);

        //todo Hard code the model details for now until we have the database set up.
        EmbeddingModelDetails modelDetails = EmbeddingModelDetails.builder()
            .modelName("all-MiniLM-L6-v2")
            .configurationVersion(EmbeddingConfigurationVersion.SPACY_PREPROCESSING_V3)
            .dimensions(384)
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

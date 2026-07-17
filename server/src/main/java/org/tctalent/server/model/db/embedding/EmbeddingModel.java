package org.tctalent.server.model.db.embedding;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tctalent.server.model.db.AbstractDomainObject;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "embedding_model")
@SequenceGenerator(name = "seq_gen", allocationSize = 1, sequenceName = "embedding_model_id_seq")
@NoArgsConstructor
public class EmbeddingModel extends AbstractDomainObject<Long> {

    //TODO JC Doc
    private String configurationVersion;
    private int dimensions;
    private String modelKey;
    private String modelName;
    private String provider;

    //TODO JC Enum?
    private String status;
}

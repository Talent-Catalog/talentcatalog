package org.tctalent.server.batchjob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;
import org.tctalent.server.logging.LogBuilder;

/**
 * Listener that implements {@link ChunkListener} to provide logging for successful chunk
 * processing and errors that occur during chunk execution.
 *
 * @author sadatmalik
 */
@Slf4j
@Component
public class LoggingChunkListener implements ChunkListener {

  @Override
  public void afterChunk(ChunkContext context) {
    String s = "";

    //Check for extra attributes added if AdaptiveDelayTasklet is used.
    if (context.hasAttribute("processingTime"))  {
      s += "Processing time: " + context.getAttribute("processingTime") + " | ";
    }
    if (context.hasAttribute("delay"))  {
      s += "Delay: " + context.getAttribute("delay") + " | ";
    }
    s += "Chunk details: " + context.getStepContext().getStepExecution();

    LogBuilder.builder(log)
        .action("Chunk processed successfully")
        .message(s)
        .logInfo();
  }

  @Override
  public void afterChunkError(ChunkContext context) {
    StepExecution stepExecution = context.getStepContext().getStepExecution();
    Throwable exception = stepExecution.getFailureExceptions().isEmpty() ? null : stepExecution.getFailureExceptions().get(0);

    LogBuilder.builder(log)
        .action("Error while processing chunk")
        .message("Chunk details: " + stepExecution + " | Error: " + (exception != null ? exception.getMessage() : "Unknown error"))
        .logError(exception);
  }
}

/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.background;

import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.util.Assert;

/**
 * Spring scheduling {@link Trigger} that schedules delays based on previous processing times.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class VariableTrigger implements Trigger {
    private final int percentageOfCpu;
    private volatile Duration initialDelay;

    public VariableTrigger(int percentageOfCpu) {
        Assert.isTrue(percentageOfCpu > 0 && percentageOfCpu < 100,
            "percentage of CPU must be > 0 and <= 100");
        this.percentageOfCpu = percentageOfCpu;
    }

    @Override
    public Instant nextExecution(TriggerContext triggerContext) {
        Instant lastExecution = triggerContext.lastScheduledExecution();
        Instant lastCompletion = triggerContext.lastCompletion();
        if (lastExecution == null || lastCompletion == null) {
            Instant instant = triggerContext.getClock().instant();
            Duration initialDelay = this.initialDelay;
            if (initialDelay == null) {
                return instant;
            }
            else {
                return instant.plus(initialDelay);
            }
        }
        Duration processingTime = Duration.between(lastExecution, lastCompletion);

        //Delay is some function of processing time
        Duration delay = computeDelay(processingTime);

        return lastCompletion.plus(delay);
    }

    private Duration computeDelay(Duration processingTime) {
        /*
          As an example, if desired percentageOfCPU is 50% then the delay will match the processing
          time. (100-50)/50 = 1.

          The minimum percentage is 1%, which means a long delay 99 times the processing time.
          (100-1)/1 = 99.

          The maximum percentage is 100%, in which case there will be no delay. (100-100)/100 = 0.

          So the delay ranges from 0 to 99 times the last processing time.
         */

        return processingTime
            .multipliedBy(100 - percentageOfCpu)
            .dividedBy(percentageOfCpu);
    }
}

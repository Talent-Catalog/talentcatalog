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

/**
 * This defines the interface that processors should implement in order to process large
 * amounts of work in chunks - as scheduled by a {@link BackRunner}.
 *
 * @author John Cameron
 */
public interface BackProcessor<CONTEXT> {

    /**
     * This method is called repeatedly - with gaps in between - to complete long tasks.
     *
     * <p>NB: As defined in {@link BackRunner}, any unchecked exception left to propagate will cancel
     * the entire operation. This is intended to prevent problems that might result from repeatedly
     * encountering the same bug when processing large datasets. If you want processing to continue
     * despite certain exceptions, catch them at a lower level.
     * @param context letting the processor keep track of where it is up to in its processing.
     *                The context object should be updated at the end of each call to this method
     *                to record where processing got up to so that we know where to start processing
     *                next time this method is called.
     * @return True if all processing is completed. In that case this method will not be called
     * again.
     */
    boolean process(CONTEXT context);
}

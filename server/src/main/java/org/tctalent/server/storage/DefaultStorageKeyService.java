/*
 * Copyright (c) 2026 Talent Catalog.
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
package org.tctalent.server.storage;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultStorageKeyService implements StorageKeyService {

    /**
     * Could just return a UUID - but this adds a prefix to the UUID consisting of the given
     * objectType and the first 4 characters of the UUID delimited.
     * See <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/optimizing-performance.html">
     *     Amazon S3 performance guidelines</a>
     * <p>
     * Adding prefixes allows Amazon to "shard" the data and parallelize access.
     */
    @Override
    public String newStorageKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String shard = shard(uuid);

        return shard + "/" + uuid;
    }

    private String shard(String uuid) {
        return uuid.substring(0, 2) + "/" + uuid.substring(2, 4);
    }
}

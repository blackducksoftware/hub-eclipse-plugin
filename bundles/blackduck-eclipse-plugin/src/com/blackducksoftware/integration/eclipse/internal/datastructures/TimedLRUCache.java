/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.eclipse.internal.datastructures;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimedLRUCache<T, S> {
    private final ConcurrentMap<T, S> cache;
    private final ConcurrentMap<T, Instant> expirationCache;
    private final int cacheTimeout;
    private final int cacheCapacity;
    private Optional<T> oldestKey;

    public TimedLRUCache(final int cacheCapacity, final int cacheTimeout) {
        this.cacheCapacity = cacheCapacity;
        this.cacheTimeout = cacheTimeout;
        oldestKey = Optional.empty();
        cache = new ConcurrentHashMap<>();
        expirationCache = new ConcurrentHashMap<>();
    }

    public S get(final T key) {
        removeStaleKeys();
        return cache.get(key);
    }

    public void put(final T key, final S value) {
        removeStaleKeys();

        if (cache.size() == cacheCapacity && oldestKey.isPresent()) {
            cache.remove(oldestKey.get());
            oldestKey = Optional.empty();
        }

        cache.put(key, value);
        expirationCache.put(key, Instant.now().plusMillis(cacheTimeout));

        if (!oldestKey.isPresent()) {
            oldestKey = getOldestKey();
        }
    }

    public Optional<T> getOldestKey() {
        return cache.keySet().stream().reduce((key1, key2) -> getOlderKey(key1, key2));
    }

    public T getOlderKey(final T key1, final T key2) {
        final Instant key1Expiration = expirationCache.get(key1);
        final Instant key2Expiration = expirationCache.get(key2);

        if (key1Expiration.isBefore(key2Expiration)) {
            return key1;
        }
        return key2;
    }

    public boolean isExpired(final T key) {
        return expirationCache.get(key).isBefore(Instant.now());
    }

    private void removeStaleKeys() {
        if (oldestKey.isPresent() && isExpired(oldestKey.get())) {
            oldestKey = Optional.empty();
            cache.keySet().stream()
                    .filter(key -> isExpired(key))
                    .forEach(key -> removeStaleKey(key));
            oldestKey = getOldestKey();
        }
    }

    private void removeStaleKey(final T key) {
        cache.remove(key);
        expirationCache.remove(key);
    }

}

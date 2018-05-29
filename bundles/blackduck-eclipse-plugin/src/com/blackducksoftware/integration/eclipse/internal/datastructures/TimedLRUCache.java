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

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.blackducksoftware.integration.exception.IntegrationException;

public class TimedLRUCache<T, S> {
    private final ConcurrentHashMap<T, S> cache;
    private final ConcurrentHashMap<T, Timestamp> cacheKeyTTL;
    private Timestamp oldestKeyAge;
    private final int cacheTimeout;
    private final int cacheCapacity;

    public TimedLRUCache(final int cacheCapacity, final int cacheTimeout) {
        this.cacheCapacity = cacheCapacity;
        this.cacheTimeout = cacheTimeout;
        cache = new ConcurrentHashMap<>();
        cacheKeyTTL = new ConcurrentHashMap<>();
    }

    public S get(final T key) throws IntegrationException {
        final S value = cache.get(key);
        final Timestamp staleTime = new Timestamp(System.currentTimeMillis() - cacheTimeout);
        if (oldestKeyAge != null && oldestKeyAge.before(staleTime)) {
            removeStaleKeys(staleTime);
        }
        return value;
    }

    public void put(final T key, final S value) {
        if (cache.size() == cacheCapacity) {
            cache.remove(Collections.min(cacheKeyTTL.entrySet(), new Comparator<Entry<T, Timestamp>>() {
                @Override
                public int compare(final Entry<T, Timestamp> entry1, final Entry<T, Timestamp> entry2) {
                    return entry1.getValue().getNanos() - entry2.getValue().getNanos();
                }
            }).getKey());
        }
        cache.put(key, value);
        cacheKeyTTL.put(key, new Timestamp(System.currentTimeMillis()));
    }

    private void removeStaleKeys(final Timestamp staleTime) {
        oldestKeyAge = null;
        for (final Entry<T, Timestamp> entry : cacheKeyTTL.entrySet()) {
            final T livingKey = entry.getKey();
            final Timestamp keyStaleTime = entry.getValue();
            if (keyStaleTime.before(staleTime)) {
                cache.remove(livingKey);
                cacheKeyTTL.remove(livingKey);
            } else {
                oldestKeyAge = (oldestKeyAge == null || oldestKeyAge.after(keyStaleTime)) ? keyStaleTime : oldestKeyAge;
            }
        }
    }

}

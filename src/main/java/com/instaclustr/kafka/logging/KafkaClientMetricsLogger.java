/*
Copyright 2021 Instaclustr Pty Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.instaclustr.kafka.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small logger wrapper that prefixes every log line with a constant label so
 * plugin logs are easy to spot in Kafka process logs, regardless of the SLF4J backend.
 */
public final class KafkaClientMetricsLogger {

    public static final String PREFIX = "[Apache Kafka Client Metrics Reporter Plugin] ";

    private final Logger delegate;

    private KafkaClientMetricsLogger(final Logger delegate) {
        this.delegate = delegate;
    }

    public static KafkaClientMetricsLogger getLogger(final Class<?> clazz) {
        return new KafkaClientMetricsLogger(LoggerFactory.getLogger(clazz));
    }

    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    public void trace(final String message) {
        delegate.trace(PREFIX + message);
    }

    public void trace(final String message, final Object... args) {
        delegate.trace(PREFIX + message, args);
    }

    public void trace(final String message, final Throwable t) {
        delegate.trace(PREFIX + message, t);
    }

    public void debug(final String message) {
        delegate.debug(PREFIX + message);
    }

    public void debug(final String message, final Object... args) {
        delegate.debug(PREFIX + message, args);
    }

    public void debug(final String message, final Throwable t) {
        delegate.debug(PREFIX + message, t);
    }

    public void info(final String message) {
        delegate.info(PREFIX + message);
    }

    public void info(final String message, final Object... args) {
        delegate.info(PREFIX + message, args);
    }

    public void info(final String message, final Throwable t) {
        delegate.info(PREFIX + message, t);
    }

    public void warn(final String message) {
        delegate.warn(PREFIX + message);
    }

    public void warn(final String message, final Object... args) {
        delegate.warn(PREFIX + message, args);
    }

    public void warn(final String message, final Throwable t) {
        delegate.warn(PREFIX + message, t);
    }

    public void error(final String message) {
        delegate.error(PREFIX + message);
    }

    public void error(final String message, final Object... args) {
        delegate.error(PREFIX + message, args);
    }

    public void error(final String message, final Throwable t) {
        delegate.error(PREFIX + message, t);
    }
}


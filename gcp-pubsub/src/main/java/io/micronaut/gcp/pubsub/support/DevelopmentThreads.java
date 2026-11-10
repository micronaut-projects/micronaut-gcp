/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.pubsub.support;

import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Creates what development mode retains across a restart on a thread of its own, whose stack holds no class of the
 * application. A gRPC channel keeps the stack trace of where it was built, and a class of the Pub/Sub client an
 * exception created as it is initialized: built on a stack of the application, they would keep the generation that
 * built them reachable for as long as they live. The thread also gives the threads they start the loader of the
 * Pub/Sub client as their context class loader, rather than that of the generation.
 *
 * <p>It exists only in development mode, and holds nothing: its presence tells that development mode is on. Outside
 * development mode, what it would build is built on the calling thread, as before.</p>
 *
 * @author graemerocher
 * @since 6.3.0
 */
@Internal
@Singleton
@DevelopmentActive
public final class DevelopmentThreads {

    /**
     * Calls a supplier on a thread of its own, and waits for it.
     *
     * @param what What is built, for the name of the thread
     * @param supplier What builds it
     * @param <T> The type built
     * @return What the supplier returned
     */
    public <T> T outsideTheApplication(String what, Supplier<T> supplier) {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                result.set(supplier.get());
            } catch (Throwable e) {
                failure.set(e);
            }
        }, "pubsub-dev-" + what);
        thread.setContextClassLoader(DevelopmentThreads.class.getClassLoader());
        thread.setDaemon(true);
        thread.start();
        boolean interrupted = false;
        while (true) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        Throwable e = failure.get();
        if (e instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (e instanceof Error error) {
            throw error;
        }
        if (e != null) {
            throw new IllegalStateException(e);
        }
        return result.get();
    }

    /**
     * Initializes a class on a thread of its own.
     *
     * @param className The class
     * @param loader The loader of the class
     */
    public void initialize(String className, ClassLoader loader) {
        outsideTheApplication("init", () -> {
            try {
                return Class.forName(className, true, loader);
            } catch (ClassNotFoundException | LinkageError e) {
                // not there in this version of the client: nothing to initialize
                return null;
            }
        });
    }
}

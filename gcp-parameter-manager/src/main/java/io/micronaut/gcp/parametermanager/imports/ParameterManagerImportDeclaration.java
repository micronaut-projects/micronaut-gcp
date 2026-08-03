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
package io.micronaut.gcp.parametermanager.imports;

import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.Nullable;

/**
 * Typed declaration for Google Cloud Parameter Manager config imports.
 *
 * @param path The parameter name
 * @param optional Whether the import is optional
 * @param format The optional explicit format override
 * @param credentialsLocation Optional service account credentials file path
 * @param encodedKey Optional base64 encoded credentials json
 * @param projectId Optional project id override
 * @param location Optional regional parameter location
 * @param version The parameter version to import. Parameter Manager does not support {@code latest}, so a version must always be specified
 * @since 6.1.0
 */
@Internal
record ParameterManagerImportDeclaration(String path,
                                         boolean optional,
                                         @Nullable String format,
                                         @Nullable String credentialsLocation,
                                         @Nullable String encodedKey,
                                         @Nullable String projectId,
                                         @Nullable String location,
                                         String version) {
}

/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.gcp.secretmanager.imports;

import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.Nullable;

/**
 * Typed declaration for Google Secret Manager config imports.
 *
 * @param path The secret path
 * @param optional Whether the import is optional
 * @param format The optional explicit format override
 * @param credentialsLocation Optional service account credentials file path
 * @param encodedKey Optional base64 encoded credentials json
 * @param projectId Optional project id override
 * @param location Optional regional secret location
 * @since 5.0
 */
@Internal
record SecretManagerImportDeclaration(String path,
                                      boolean optional,
                                      @Nullable String format,
                                      @Nullable String credentialsLocation,
                                      @Nullable String encodedKey,
                                      @Nullable String projectId,
                                      @Nullable String location) {
}

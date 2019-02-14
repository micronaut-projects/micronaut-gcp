/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package example;

import io.micronaut.context.env.Environment;
import io.micronaut.discovery.cloud.ComputeInstanceMetadata;
import io.micronaut.discovery.cloud.gcp.GoogleComputeInstanceMetadataResolver;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.validation.Validated;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@Controller("/")
@Validated
public class HelloController {
    private static final Logger LOG = LoggerFactory.getLogger(HelloController.class);
    private final ApplicationConfiguration applicationConfiguration;
    private final Environment environment;
    private final AnotherService anotherService;
    private Map<String, String> metadata = Collections.emptyMap();

    public HelloController(
            ApplicationConfiguration applicationConfiguration,
            Environment environment,
            AnotherService anotherService) {
        this.applicationConfiguration = applicationConfiguration;
        this.environment = environment;
        this.anotherService =anotherService;
    }

    @Get(uri = "/traced", produces = MediaType.TEXT_PLAIN)
    public String trace() {
        return anotherService.go();
    }

    @Get(uri = "/instance", produces = MediaType.TEXT_PLAIN)
    public Single<String> instance() {
        return Single.just("Instance: " + applicationConfiguration.getInstance().getId().orElse(null));
    }

    @Get(uri = "/env", produces = MediaType.TEXT_PLAIN)
    public List<String> env() {
        return new ArrayList<>(environment.getActiveNames());
    }

    @Get(uri = "/metadata", produces = MediaType.TEXT_PLAIN)
    public Map<String, String> metadata() {
        return metadata;
    }


    @EventListener
    public void onStartup(ServiceStartedEvent serviceStartedEvent) {
        final Map<String, String> metadata = serviceStartedEvent.getSource().getMetadata().asMap();
        LOG.info("Received metadata {} ", metadata);
        this.metadata = metadata;
    }
}

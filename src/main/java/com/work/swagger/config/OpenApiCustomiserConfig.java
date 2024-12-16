package com.work.swagger.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 *
 * @author ajuar
 */
@Component
@Slf4j
public class OpenApiCustomiserConfig implements OpenApiCustomizer {

    private static final String SWAGGER_PATH_DEFAULT = "/";

    private final HttpServletRequest request;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private ObjectMapper objectMapper;

    public OpenApiCustomiserConfig(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void customise(OpenAPI openApi) {
        Map<String, SwaggerExamples> resources = loadResources("swagger_examples");
        openApi.getServers().clear();
        openApi.addServersItem(new Server().url(getUrl(this.request)));

        openApi.getPaths().values().forEach(pathItem
                -> pathItem.readOperations().forEach(operation -> {
                    String tagName = operation.getTags().get(0);
                    log.debug("{}-{}", tagName, operation.getOperationId());
                    if (resources.containsKey(tagName)) {
                        log.debug("Found: {}", resources.get(tagName));
                        assignExamples(operation, resources.get(tagName));
                    }
                })
        );
    }

    private String getUrl(HttpServletRequest request) {
        try {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            return (serverPort == 80 || serverPort == 443)
                    ? String.format("%s://%s%s", scheme, serverName, contextPath)
                    : String.format("%s://%s:%d%s", scheme, serverName, serverPort, contextPath);
        } catch (Exception ex) {
            log.error("Error Url: {}", ex.getMessage());
            return SWAGGER_PATH_DEFAULT;
        }
    }

    private Map<String, SwaggerExamples> loadResources(String directory) {
        Map<String, SwaggerExamples> mapResources = new HashMap<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:" + directory + "/*");
            for (Resource resource : resources) {
                SwaggerExamples examples = loadResponseExamplesFromJson(resource);
                mapResources.put(examples.tagName(), examples);
            }
        } catch (IOException ex) {
            log.error("{}", ex.getMessage());
        }
        return mapResources;
    }

    private Content createContent(Map<String, Example> examples, String ref) {
        return new Content().addMediaType("application/json", new MediaType().examples(examples).schema(new Schema<>().$ref(ref)));
    }

    private Content createContent(Map<String, Example> examples) {
        return new Content().addMediaType("application/json", new MediaType().examples(examples));
    }

    private RequestBody createRequest(Map<String, Example> examples) {
        return new RequestBody().description("Default description").content(createContent(examples));
    }

    private ApiResponse createResponse(Map<String, Example> examples, String ref) {
        return new ApiResponse().description("Default description").content(createContent(examples, ref));
    }

    private Example createExample(Object value, String description) {
        Example example = new Example();
        example.setValue(value);
        example.setDescription(description);
        return example;
    }

    private String getRef(Operation operation, String statusCode) {
        log.info("Status: {}", statusCode);
        
        if (!operation.getResponses().containsKey(statusCode)) {
            return "#/components/schemas/response";
        }
        
        return operation.getResponses().get(statusCode).getContent().get("application/json").getSchema().get$ref();
    }

    private void assignExamples(Operation operation, SwaggerExamples examplesForOperation) {

        Map<String, Example> examplesRequest = new HashMap<>();
        ApiResponses apiResponses = new ApiResponses();
        examplesForOperation.operations.forEach((SwaggerOperation op) -> {
            if (op.id.equalsIgnoreCase(operation.getOperationId())) {
                op.requests.forEach((var req)
                        -> examplesRequest.put(req.description, createExample(req.example, req.description))
                );
                if (!examplesRequest.isEmpty()) {
                    var requestBody = createRequest(examplesRequest);
                    operation.setRequestBody(requestBody);
                }
                op.responses.forEach((SwaggerExample res) -> {
                    String ref = getRef(operation, String.valueOf(res.statusCode));
                    log.info("Ref: {}", ref);
                    Map<String, Example> examplesResponse = new HashMap<>();
                    examplesResponse.put(res.description, createExample(res.example, res.description));
                    var apiResponse = createResponse(examplesResponse, ref);
                    apiResponses.addApiResponse(String.valueOf(res.statusCode), apiResponse);
                    operation.setResponses(apiResponses);
                });
                if (!apiResponses.isEmpty()) {
                    operation.setResponses(apiResponses);
                }
            }
        });
    }

    private SwaggerExamples loadResponseExamplesFromJson(Resource resource) throws IOException {
        return objectMapper.readValue(resource.getInputStream(), SwaggerExamples.class);
    }

    private record SwaggerExamples(String tagName, List<SwaggerOperation> operations) {

        public SwaggerExamples(String tagName, List<SwaggerOperation> operations) {
            this.tagName = tagName;
            this.operations = Optional.ofNullable(operations).orElseGet(ArrayList::new);
        }
    }

    private record SwaggerOperation(String id, List<SwaggerExample> requests, List<SwaggerExample> responses) {

        public SwaggerOperation(String id, List<SwaggerExample> requests, List<SwaggerExample> responses) {
            this.id = id;
            this.requests = Optional.ofNullable(requests).orElseGet(ArrayList::new);
            this.responses = Optional.ofNullable(responses).orElseGet(ArrayList::new);
        }
    }

    private record SwaggerExample(Integer statusCode, String description, Object example) {

    }
}

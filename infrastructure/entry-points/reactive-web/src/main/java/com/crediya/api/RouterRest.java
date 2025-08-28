package com.crediya.api;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.ErrorResponse;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.crediya.api.constants.Path.APPLICATION_PATH;
import static com.crediya.api.constants.swagger.DocApi.*;
import static com.crediya.api.constants.swagger.creditapplication.CreditApplicationDocApi.DESCRIPTION_CREATED;
import static com.crediya.api.constants.swagger.creditapplication.CreditApplicationDocApi.SUMMARY_CREATE;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
 
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(APPLICATION_PATH), handler::createApplication);
    }
    
    @Bean
    public OpenAPI apiDocumentation() {
        return new OpenAPI()
            .info(new Info()
                .title(TITLE)
                .description(DESCRIPTION)
                .version(VERSION))
            .components(new Components()
                .addSchemas(CreateCreditApplicationRequest.class.getName(),
                    createSchemaFromClass(CreateCreditApplicationRequest.class))
                .addSchemas(CreditApplicationApiResponse.class.getName(),
                    createSchemaFromClass(CreditApplicationApiResponse.class))
                .addSchemas(ErrorResponse.class.getName(),
                    createSchemaFromClass(ErrorResponse.class))
            )
            .paths(new Paths()
                .addPathItem(APPLICATION_PATH, new PathItem()
                    .post(new Operation()
                        .summary(SUMMARY_CREATE)
                        .description(DESCRIPTION_CREATED)
                        .requestBody(new RequestBody()
                            .required(true)
                            .content(new Content()
                                .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                .schema(new Schema<CreateCreditApplicationRequest>()
                                    .$ref("#/components/schemas/" + CreateCreditApplicationRequest.class.getName())))
                            ))
                        .responses(new ApiResponses()
                            .addApiResponse("201", new ApiResponse()
                                .description(DESCRIPTION_CREATED)
                                .content(new Content()
                                    .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<CreditApplicationApiResponse>()))))
                            .addApiResponse("500", new ApiResponse()
                                .description(DESCRIPTION_INTERNAL_ERROR)
                                .content(new Content()
                                    .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<ErrorResponse>()))))
                            .addApiResponse("400", new ApiResponse()
                                .description(DESCRIPTION_BAD_REQUEST)
                                .content(new Content()
                                    .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new Schema<ErrorResponse>()))))
                            .addApiResponse("404", new ApiResponse()
                                .description(DESCRIPTION_NOT_FOUND)
                                .content(new Content()
                                    .addMediaType(MediaType.APPLICATION_JSON_VALUE, new io.swagger.v3.oas.models.media.MediaType()
                                        .schema(new ObjectSchema()
                                            .addProperty("message", new StringSchema().description("Error message"))
                                            .addProperty("code", new NumberSchema().description("Error codigo")))))))
                    )));
    }
    
    @SuppressWarnings("rawtypes")
    private Schema createSchemaFromClass(Class<?> clazz) {
        Schema schema = new Schema();
        schema.setType("object");
        schema.setName(clazz.getSimpleName());
        
        return schema;
    }
}

package com.redhat.consulting.demos;

import com.redhat.consulting.demos.entity.NotAuthenticatedResponseEntity;
import com.redhat.consulting.demos.entity.PostEntity;
import com.redhat.consulting.demos.entity.PostResponseEntity;
import com.redhat.consulting.demos.security.NotAuthenticatedException;
import com.redhat.consulting.demos.security.SecurityProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

@Component
public class RestRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        onException(NotAuthenticatedException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(401))
                .setBody().constant(new NotAuthenticatedResponseEntity())
                .marshal().json(JsonLibrary.Jackson)
                .stop();

        restConfiguration()
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "A simple REST API with authentication")
                .apiProperty("api.version", "v1")
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.json);

        rest("/api")
                .securityDefinitions()
                    .basicAuth("basicAuth")
                .end()

                .post("/process")
                    .description("Inserts a new post")
                    .type(PostEntity.class)
                    .outType(PostResponseEntity.class)
                    .security("basicAuth")
                    .responseMessage()
                        .message("Not Authenticated").code(401).responseModel(NotAuthenticatedResponseEntity.class)
                    .endResponseMessage()
                    .route().routeId("api-process")
                        .process(new SecurityProcessor())
                        .to("direct:process-request")
                    .endRest();

        from("direct:process-request")
                .log("Incoming, unmarshalled request: ${body}")
                .marshal().json(JsonLibrary.Jackson)
                // We bridge endpoints so that we can propagate the response code from the other service
                .to("https://jsonplaceholder.typicode.com/posts?bridgeEndpoint=true")
                .unmarshal().json(JsonLibrary.Jackson, PostResponseEntity.class)
                .log("Outcome from service (marshalled): ${body}");

    }
}

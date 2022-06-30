package com.chatbot.adjustment.config;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Value("${login.oauth-token.url}")
	private String accessTokenUri;

	public static final String securitySchemaOAuth2 = "oauth2schema";
	public static final String authorizationScopeGlobal = "global";
	public static final String authorizationScopeGlobalDesc ="accessEverything";

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.chatbot.adjustment.web.controller.rest"))
				.paths(PathSelectors.any())
				.build()
				.securityContexts(Collections.singletonList(securityContext()))
				.securitySchemes(Arrays.asList(securitySchema()))
				.apiInfo(apiInfo());
	}
	
	@Bean
	public SecurityScheme apiKey() {
		return new ApiKey(HttpHeaders.AUTHORIZATION, "apiKey", "header");
	}

	private OAuth securitySchema() {
		List<AuthorizationScope> authorizationScopeList = newArrayList();
		authorizationScopeList.add(new AuthorizationScope("read", ""));
		authorizationScopeList.add(new AuthorizationScope("write", ""));
		authorizationScopeList.add(new AuthorizationScope("homepage", ""));

		List<GrantType> grantTypes = newArrayList();
		GrantType passwordCredentialsGrant = new ResourceOwnerPasswordCredentialsGrant(accessTokenUri);
		grantTypes.add(passwordCredentialsGrant);

		return new OAuth("oauth2", authorizationScopeList, grantTypes);
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().
				securityReferences(defaultAuth())
				.forPaths(PathSelectors.regex("/api/auth/*"))
				.build();
	}

	private List<SecurityReference> defaultAuth() {

		final AuthorizationScope[] authorizationScopes = new AuthorizationScope[3];
		authorizationScopes[0] = new AuthorizationScope("read", "");
		authorizationScopes[1] = new AuthorizationScope("write", "");
		authorizationScopes[2] = new AuthorizationScope("homepage", "");

		return Collections.singletonList(new SecurityReference("oauth2", authorizationScopes));
	}

	@Bean
	public SecurityConfiguration security() {

		//return new SecurityConfiguration
		//		("client", "secret", "", "", "Bearer access token", ApiKeyVehicle.HEADER, HttpHeaders.AUTHORIZATION,"");

		return SecurityConfigurationBuilder.builder()
				.clientId("client-id")
				.clientSecret("client-secret")
				.realm("")
				.appName("")
				.scopeSeparator(",")
				.additionalQueryStringParams(null)
				.useBasicAuthenticationWithAccessCodeGrant(false)
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfo(
				"로그인 서버사용 API",
				"초기에 정산 시스템 목적으로 만들었으나, 로그인 서버를 사용하는 API를 제공합니다.",
				"API 0.1",
				"",
				new Contact("아이봇개발팀", "", "ai@enliple.com"),
				"", "", Collections.emptyList());
	}

	@Bean
	UiConfiguration uiConfig() {
		return UiConfigurationBuilder.builder()
				.deepLinking(true)
				.displayOperationId(false)
				.defaultModelsExpandDepth(1)
				.defaultModelExpandDepth(1)
				.defaultModelRendering(ModelRendering.EXAMPLE)
				.displayRequestDuration(false)
				.docExpansion(DocExpansion.NONE)
				.filter(false)
				.maxDisplayedTags(null)
				.operationsSorter(OperationsSorter.ALPHA)
				.showExtensions(false)
				.tagsSorter(TagsSorter.ALPHA)
				.validatorUrl(null)
				.build();
	}
}

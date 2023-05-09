/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.tracing.otlp;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OtlpAutoConfiguration}.
 *
 * @author Jonatan Ivanov
 */
class OtlpAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(OtlpAutoConfiguration.class));

	@Test
	void shouldSupplyBeans() {
		this.contextRunner.run((context) -> assertThat(context).hasSingleBean(OtlpHttpSpanExporter.class)
			.hasSingleBean(SpanExporter.class));
	}

	@Test
	void shouldNotSupplyBeansIfTracingIsDisabled() {
		this.contextRunner.withPropertyValues("management.tracing.enabled=false")
			.run((context) -> assertThat(context).doesNotHaveBean(SpanExporter.class));
	}

	@Test
	void shouldNotSupplyBeansIfTracingBridgeIsMissing() {
		this.contextRunner.withClassLoader(new FilteredClassLoader("io.micrometer.tracing"))
			.run((context) -> assertThat(context).doesNotHaveBean(SpanExporter.class));
	}

	@Test
	void shouldNotSupplyBeansIfOtelSdkIsMissing() {
		this.contextRunner.withClassLoader(new FilteredClassLoader("io.opentelemetry.sdk"))
			.run((context) -> assertThat(context).doesNotHaveBean(SpanExporter.class));
	}

	@Test
	void shouldNotSupplyBeansIfOtelApiIsMissing() {
		this.contextRunner.withClassLoader(new FilteredClassLoader("io.opentelemetry.api"))
			.run((context) -> assertThat(context).doesNotHaveBean(SpanExporter.class));
	}

	@Test
	void shouldNotSupplyBeansIfExporterIsMissing() {
		this.contextRunner.withClassLoader(new FilteredClassLoader("io.opentelemetry.exporter"))
			.run((context) -> assertThat(context).doesNotHaveBean(SpanExporter.class));
	}

	@Test
	void shouldBackOffWhenCustomHttpExporterIsDefined() {
		this.contextRunner.withUserConfiguration(CustomHttpExporterConfiguration.class)
			.run((context) -> assertThat(context).hasBean("customOtlpHttpSpanExporter")
				.hasSingleBean(SpanExporter.class));
	}

	@Test
	void shouldBackOffWhenCustomGrpcExporterIsDefined() {
		this.contextRunner.withUserConfiguration(CustomGrpcExporterConfiguration.class)
			.run((context) -> assertThat(context).hasBean("customOtlpGrpcSpanExporter")
				.hasSingleBean(SpanExporter.class));
	}

	@Configuration(proxyBeanMethods = false)
	private static class CustomHttpExporterConfiguration {

		@Bean
		OtlpHttpSpanExporter customOtlpHttpSpanExporter() {
			return OtlpHttpSpanExporter.builder().build();
		}

	}

	@Configuration(proxyBeanMethods = false)
	private static class CustomGrpcExporterConfiguration {

		@Bean
		OtlpGrpcSpanExporter customOtlpGrpcSpanExporter() {
			return OtlpGrpcSpanExporter.builder().build();
		}

	}

}

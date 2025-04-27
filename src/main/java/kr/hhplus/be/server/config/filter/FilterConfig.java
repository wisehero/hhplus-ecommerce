package kr.hhplus.be.server.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import kr.hhplus.be.server.filter.HttpLoggingFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<HttpLoggingFilter> loggingFilter(HttpLoggingFilter filter) {
		FilterRegistrationBean<HttpLoggingFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(filter);
		registration.addUrlPatterns("/*");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}

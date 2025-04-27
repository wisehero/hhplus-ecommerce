package kr.hhplus.be.server.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpLoggingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		long start = System.currentTimeMillis();
		try {
			filterChain.doFilter(requestWrapper, responseWrapper);
		} finally {
			long duration = System.currentTimeMillis() - start;
			logRequest(requestWrapper);
			logResponse(responseWrapper, duration);
			// 응답 바디를 복원해서 클라이언트로 전송
			responseWrapper.copyBodyToResponse();
		}
	}

	private void logRequest(ContentCachingRequestWrapper req) {
		StringBuilder sb = new StringBuilder("\n--- HTTP REQUEST ---\n");
		sb.append(req.getMethod()).append(" ").append(req.getRequestURI());
		if (req.getQueryString() != null)
			sb.append("?").append(req.getQueryString());
		sb.append("\nHeaders:\n");
		Collections.list(req.getHeaderNames()).forEach(name ->
			sb.append("  ").append(name).append(": ").append(req.getHeader(name)).append("\n")
		);
		byte[] buf = req.getContentAsByteArray();
		if (buf.length > 0) {
			String payload = new String(buf, StandardCharsets.UTF_8);
			sb.append("Body:\n").append(payload).append("\n");
		}
		sb.append("--------------------");
		log.info(sb.toString());
	}

	private void logResponse(ContentCachingResponseWrapper res, long duration) {
		StringBuilder sb = new StringBuilder("\n--- HTTP RESPONSE ---\n");
		sb.append("Status: ").append(res.getStatus()).append(", time=").append(duration).append("ms\n");
		sb.append("Headers:\n");
		res.getHeaderNames().forEach(name ->
			sb.append("  ").append(name).append(": ").append(res.getHeader(name)).append("\n")
		);
		byte[] buf = res.getContentAsByteArray();
		if (buf.length > 0) {
			String payload = new String(buf, StandardCharsets.UTF_8);
			sb.append("Body:\n").append(payload).append("\n");
		}
		sb.append("---------------------");
		log.info(sb.toString());
	}
}

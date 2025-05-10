package dev.santhoshle.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
	private final String method;
	private final String target;
	private final String httpVersion;
	private final Map<String, String> headers = new HashMap<>();
	private final String body;

	public Request(List<String> requestLines, String body) {
		String[] parts = requestLines.get(0).split(" ");
		this.method = parts[0];
		this.target = parts[1];
		this.httpVersion = parts.length > 2 ? parts[2] : HttpConstants.HTTP_VERSION;
		this.body = body;
		parseHeaders(requestLines);
	}

	private void parseHeaders(List<String> lines) {
		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line == null || line.trim().isEmpty())
				break;

			int index = line.indexOf(":");
			if (index > 0) {
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				headers.put(key, value);
			}
		}
	}

	public String getMethod() {
		return method;
	}

	public String getTarget() {
		return target;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public String getBody() {
		return body;
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}
}

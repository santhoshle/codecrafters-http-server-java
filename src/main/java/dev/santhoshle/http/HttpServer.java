package dev.santhoshle.http;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HttpServer {
	private static final int PORT = 4221;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			serverSocket.setReuseAddress(true);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				new Thread(() -> handleConnection(clientSocket, args)).start();
			}
		} catch (IOException e) {
			System.err.println("Server error: " + e.getMessage());
		}
	}

	private static void handleConnection(Socket clientSocket, String[] args) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				OutputStream clientResponse = clientSocket.getOutputStream()) {

			while (!clientSocket.isClosed()) {
				List<String> requestLines = new ArrayList<>();
				String line;
				while ((line = reader.readLine()) != null && !line.isEmpty()) {
					requestLines.add(line);
				}

				if (requestLines.isEmpty())
					break;

				String contentLengthValue = requestLines.stream()
						.filter(l -> l.toLowerCase().startsWith(HttpConstants.HEADER_CONTENT_LENGTH.toLowerCase()))
						.map(l -> l.substring(15).trim()).findFirst().orElse(null);

				String methodLine = requestLines.get(0);
				boolean isPost = methodLine.startsWith(HttpConstants.HTTP_METHOD_POST);
				StringBuilder bodyBuilder = new StringBuilder();

				if (isPost && contentLengthValue != null) {
					int contentLength = Integer.parseInt(contentLengthValue);
					char[] bodyChars = new char[contentLength];
					int read = reader.read(bodyChars, 0, contentLength);
					bodyBuilder.append(bodyChars, 0, read);
				}

				Request request = new Request(requestLines, bodyBuilder.toString());
				Response response = handleRequest(request, args);

				clientResponse.write(response.getHeaders().getBytes(StandardCharsets.UTF_8));
				if (response.getBody().length > 0) {
					clientResponse.write(response.getBody());
				}
				clientResponse.flush();

				String connectionHeader = request.getHeader(HttpConstants.HEADER_CONNECTION);
				if (connectionHeader != null && connectionHeader.equalsIgnoreCase(HttpConstants.CONNECTION_CLOSE)) {
					break;
				}
			}
		} catch (IOException e) {
			System.err.println("Connection handling error: " + e.getMessage());
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("Error closing client socket: " + e.getMessage());
			}
		}
	}

	private static Response handleRequest(Request request, String[] args) {
		Response response = new Response();
		StringBuilder responseBuilder = new StringBuilder(HttpConstants.HTTP_VERSION).append(" ");

		String target = request.getTarget();
		String method = request.getMethod();

		if (HttpConstants.HTTP_METHOD_GET.equals(method)) {
			if ("/".equals(target) || "/index.html".equals(target)) {
				responseBuilder.append(HttpConstants.HTTP_STATUS_200).append(HttpConstants.HTTP_EOL);
				responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
						.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
						.append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
			} else if (target.startsWith(HttpConstants.HTTP_URL_ECHO)) {
				String message = target.substring(HttpConstants.HTTP_URL_ECHO.length());
				responseBuilder.append(HttpConstants.HTTP_STATUS_200).append(HttpConstants.HTTP_EOL);
				responseBuilder.append(HttpConstants.HEADER_CONTENT_TYPE).append(HttpConstants.HEADER_SEPERATOR)
						.append(HttpConstants.CONTENT_TYPE_TEXT).append(HttpConstants.HTTP_EOL);

				List<String> encodings = Optional.ofNullable(request.getHeader(HttpConstants.HEADER_ACCEPT_ENCODING))
						.map(val -> Arrays.stream(val.split(",")).map(String::trim).collect(Collectors.toList()))
						.orElse(Collections.emptyList());

				if (encodings.contains(HttpConstants.ENCODING_GZIP)) {
					byte[] messageCompressed = Utils.getGzipCompressedMessage(message);
					responseBuilder.append(HttpConstants.HEADER_CONTENT_ENCODING).append(HttpConstants.HEADER_SEPERATOR)
							.append(HttpConstants.ENCODING_GZIP).append(HttpConstants.HTTP_EOL);
					responseBuilder.append(HttpConstants.HEADER_CONTENT_LENGTH).append(HttpConstants.HEADER_SEPERATOR)
							.append(messageCompressed.length).append(HttpConstants.HTTP_EOL);
					responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
							.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
							.append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
					response.setBody(messageCompressed);
				} else {
					responseBuilder.append(HttpConstants.HEADER_CONTENT_LENGTH).append(HttpConstants.HEADER_SEPERATOR)
							.append(message.length()).append(HttpConstants.HTTP_EOL);
					responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
							.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
							.append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
					responseBuilder.append(message);
				}
			} else if (HttpConstants.HTTP_URL_UA.equals(target)) {
				String ua = request.getHeader(HttpConstants.HEADER_USER_AGENT);
				responseBuilder.append(HttpConstants.HTTP_STATUS_200).append(HttpConstants.HTTP_EOL)
						.append(HttpConstants.HEADER_CONTENT_TYPE).append(HttpConstants.HEADER_SEPERATOR)
						.append(HttpConstants.CONTENT_TYPE_TEXT).append(HttpConstants.HTTP_EOL);
				responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
						.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
						.append(HttpConstants.HTTP_EOL);
				
				if (ua != null) {
					responseBuilder.append(HttpConstants.HEADER_CONTENT_LENGTH).append(HttpConstants.HEADER_SEPERATOR)
							.append(ua.length()).append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
					responseBuilder.append(ua);
				} else {
					responseBuilder.append(HttpConstants.HEADER_CONTENT_LENGTH).append(HttpConstants.HEADER_SEPERATOR)
							.append(0).append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
				}
			} else if (target.startsWith(HttpConstants.FILE_PATH)) {
				String fileName = target.substring(HttpConstants.FILE_PATH.length());
				String dir = Utils.getEnvProperty(HttpConstants.DIR_PATH, args);
				Path filePath = Paths.get(dir, fileName);

				if (Files.exists(filePath)) {
					try {
						String content = Files.readString(filePath);
						responseBuilder.append(HttpConstants.HTTP_STATUS_200).append(HttpConstants.HTTP_EOL)
								.append(HttpConstants.HEADER_CONTENT_TYPE).append(HttpConstants.HEADER_SEPERATOR)
								.append(HttpConstants.CONTENT_TYPE_OCTET_STREAM).append(HttpConstants.HTTP_EOL)
								.append(HttpConstants.HEADER_CONTENT_LENGTH).append(HttpConstants.HEADER_SEPERATOR)
								.append(content.length()).append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL)
								.append(content);
					} catch (IOException e) {
						responseBuilder.append(HttpConstants.HTTP_STATUS_500).append(HttpConstants.HTTP_EOL)
								.append(HttpConstants.HTTP_EOL);
					}
				} else {
					responseBuilder.append(HttpConstants.HTTP_STATUS_404).append(HttpConstants.HTTP_EOL)
							.append(HttpConstants.HTTP_EOL);
				}

				responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
						.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
						.append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
			} else {
				responseBuilder.append(HttpConstants.HTTP_STATUS_404).append(HttpConstants.HTTP_EOL);
				responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
						.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
						.append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
			}
		} else if (HttpConstants.HTTP_METHOD_POST.equals(method) && target.startsWith(HttpConstants.FILE_PATH)) {
			String fileName = target.substring(HttpConstants.FILE_PATH.length());
			String dir = Utils.getEnvProperty(HttpConstants.DIR_PATH, args);
			Path filePath = Paths.get(dir, fileName);

			try {
				Files.createDirectories(filePath.getParent());
				Files.write(filePath, request.getBody().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING);
				responseBuilder.append(HttpConstants.HTTP_STATUS_201).append(HttpConstants.HTTP_EOL);
			} catch (IOException e) {
				responseBuilder.append(HttpConstants.HTTP_STATUS_500).append(HttpConstants.HTTP_EOL);
			}

			responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
					.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
					.append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
		} else {
			responseBuilder.append(HttpConstants.HTTP_STATUS_404).append(HttpConstants.HTTP_EOL);
			responseBuilder.append(HttpConstants.HEADER_CONNECTION).append(HttpConstants.HEADER_SEPERATOR)
					.append(getConnectionResponseHeader(request.getHeader(HttpConstants.HEADER_CONNECTION)))
					.append(HttpConstants.HTTP_EOL).append(HttpConstants.HTTP_EOL);
		}

		response.setHeaders(responseBuilder.toString());
		System.out.println("Sending the response : "+responseBuilder.toString());
		return response;
	}

	private static String getConnectionResponseHeader(String connectionHeader) {
		if (connectionHeader != null && connectionHeader.equalsIgnoreCase(HttpConstants.CONNECTION_CLOSE)) {
			return HttpConstants.CONNECTION_CLOSE;
		}
		return HttpConstants.CONNECTION_KEEP_ALIVE;
	}
}

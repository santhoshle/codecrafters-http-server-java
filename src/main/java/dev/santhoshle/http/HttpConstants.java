package dev.santhoshle.http;

public class HttpConstants {
	
	public static final String HTTP_VERSION = "HTTP/1.1";
	
	public static final String HTTP_EOL = "\r\n";
	
	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_POST = "POST";
	
	
	public static final String HTTP_STATUS_200 = "200 OK";
	public static final String HTTP_STATUS_201 = "201 Created";
	public static final String HTTP_STATUS_404 = "404 Not Found";
	public static final String HTTP_STATUS_500 = "500 Internal Server Error";
	
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONNECTION = "Connection";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_SEPERATOR = ": ";
    
    public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    public static final String CONNECTION_CLOSE = "close";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
    public static final String ENCODING_GZIP = "gzip";
    
    public static final String HTTP_URL_ECHO = "/echo/";
    public static final String HTTP_URL_UA = "/user-agent";
    
    public static final String FILE_PATH = "/files/";
    public static final String DIR_PATH = "--directory";
    

    private HttpConstants() {}
}

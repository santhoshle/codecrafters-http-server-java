package dev.santhoshle.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class Utils {
	
	public static byte[] getGzipCompressedMessage(String message) {
		if (message == null || message.isEmpty())
			return new byte[0];

		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {

			gzipStream.write(message.getBytes(StandardCharsets.UTF_8));
			gzipStream.finish();
			return byteStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}
	
	public static String getEnvProperty(String envKey, String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(envKey) && i + 1 < args.length) {
                return args[i + 1];
            } else if (args[i].startsWith(envKey)) {
                return args[i].substring(envKey.length());
            }
        }
        return "/tmp/";
    }
}

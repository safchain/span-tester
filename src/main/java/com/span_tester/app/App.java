package com.span_tester.app;

import com.span_tester.grpc.SecurityModuleGrpc;
import com.span_tester.grpc.SecurityModuleGrpc;
import com.span_tester.grpc.AppAnnotationParams;

import sun.management.VMManagement;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;

import java.io.File;
import java.io.IOException;
import jdk.jfr.EventType;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.lang.reflect.Method;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import java.lang.InterruptedException;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.NettyChannelBuilder;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import java.util.Set;
import java.net.*;
import java.io.*;

import datadog.trace.api.Trace;

public class App implements Runnable {
	// port to listen connection
	static final int PORT = 8080;

	// Client Connection via Socket Class
	private Socket connect;

	public App(Socket c) {
		connect = c;
	}

	public static void main(String[] args) {
		try {
			// enforce();

			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.");

			while (true) {
				App myServer = new App(serverConnect.accept());

				Thread thread = new Thread(myServer);
				thread.start();
			}
		} catch (IOException e) {
			System.err.println("Server connection error : " + e.getMessage());
		}
	}

	@Trace(operationName = "head")
	// @Cws(domainName = "github.com")
	private void head(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(fileRequested);

		getUrl("https:/" + file.toString());

		out.println("HTTP/1.1 200 OK");
		out.println("Server: APM/CWS demo");
		out.println();
		out.flush();

		dataOut.flush();
	}

	@Trace(operationName = "getSecret")
	// @Cws(fileField = "open.file.path", fileValue = "/tmp/secret")
	private void getSecret(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(fileRequested);

		file.createNewFile();

		int fileLength = (int) file.length();
		String content = getContentType(fileRequested);
		byte[] fileData = readFileData(file, fileLength);

		out.println("HTTP/1.1 200 OK");
		out.println("Server: APM/CWS demo");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println();
		out.flush();

		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
	}

	@Trace
	private void processRequest(BufferedReader in, PrintWriter out, OutputStream dataOut) throws IOException {
		String fileRequested = null;
		try {
			String input = in.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			fileRequested = parse.nextToken().toLowerCase();

			if (method.equals("GET")) {
				getSecret(out, dataOut, fileRequested);
			} else if (method.equals("HEAD")) {
				head(out, dataOut, fileRequested);
			} else {
				notImplemented(out, dataOut, fileRequested);
			}

		} catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Error with file not found exception : " + ioe.getMessage());
			}
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		}
	}

	@Trace
	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;
		BufferedOutputStream dataOut = null;

		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());

			processRequest(in, out, dataOut);
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		}

		try {
			in.close();
			out.close();
			dataOut.close();
			connect.close();
		} catch (Exception e) {
			System.err.println("Error closing stream : " + e.getMessage());
		}
	}

	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}

	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}

	private void notImplemented(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		String contentMimeType = "text/html";
		out.println("HTTP/1.1 501 Not Implemented");
		out.println("Server: APM/CWS demo");
		out.println("Date: " + new Date());
		out.println("Content-type: " + contentMimeType);
		out.println();
		out.flush();
		dataOut.flush();
	}

	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
		String content = "text/html";
		out.println("HTTP/1.1 404 File Not Found");
		out.println("Server: APM/CWS demo");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println();
		out.flush();
		dataOut.flush();
	}

	/*
	 * private static void enforce() {
	 * List allParams = new ArrayList();
	 * 
	 * Class<App> metadata = App.class;
	 * for ( Method method : metadata.getDeclaredMethods() ) {
	 * Cws annotation = method.getAnnotation( Cws.class );
	 * if ( annotation == null )
	 * continue;
	 * 
	 * String spanName = method.getName();
	 * if (annotation.operationName().length() > 0) {
	 * spanName = annotation.operationName();
	 * }
	 * 
	 * AppAnnotationParams params = AppAnnotationParams.newBuilder()
	 * .setSpanName(spanName)
	 * .setFileField(annotation.fileField())
	 * .setFileValue(annotation.fileValue())
	 * .setDomainName(annotation.domainName())
	 * .setProcessPath("")
	 * .setPID(getCurrrentProcessId())
	 * .build();
	 * 
	 * allParams.add(params);
	 * 
	 * //System.out.printf( "%s %s %s\n",
	 * annotation.operationName(),
	 * annotation.fileField(),
	 * annotation.fileValue()
	 * );
	 * }
	 * 
	 * ManagedChannel channel = NettyChannelBuilder.forAddress(new
	 * DomainSocketAddress("/opt/datadog-agent/run/runtime-security.sock"))
	 * .eventLoopGroup(new EpollEventLoopGroup())
	 * .channelType(EpollDomainSocketChannel.class)
	 * .usePlaintext()
	 * .build();
	 * 
	 * try {
	 * Client client = new Client(channel);
	 * client.submit(allParams);
	 * }
	 * finally {
	 * try {
	 * channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
	 * }
	 * catch(java.lang.InterruptedException e) {
	 * }
	 * }
	 * }
	 */

	private static int getCurrrentProcessId() {

		try {
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);

			VMManagement management = (VMManagement) jvm.get(runtime);
			Method method = management.getClass().getDeclaredMethod("getProcessId");
			method.setAccessible(true);

			return (Integer) method.invoke(management);
		} catch (Exception e) {
			return 0;
		}
	}

	private static void getUrl(String url) {
		try {
			URL u = new URL(url);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(u.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				System.out.println(inputLine);
			in.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}

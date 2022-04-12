package com.span_tester.app;

import java.lang.Iterable;
import com.span_tester.grpc.SecurityModuleGrpc;
import com.span_tester.grpc.Api;
import com.span_tester.grpc.AppAnnotationParams;
import com.span_tester.grpc.ParseAppAnnotationParams;
import com.span_tester.grpc.ParseAppAnnotationMessage;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class Client {
	private final SecurityModuleGrpc.SecurityModuleBlockingStub blockingStub;

	public Client(Channel channel) {
		blockingStub = SecurityModuleGrpc.newBlockingStub(channel);
	}
  
    public void submit(Iterable<AppAnnotationParams> allParams) {
        ParseAppAnnotationParams request = ParseAppAnnotationParams.newBuilder().addAllParams(allParams).build();
        ParseAppAnnotationMessage response;
        try {
            response = blockingStub.parseAppAnnotation(request);
        } catch (StatusRuntimeException e) {
            System.out.println(e.toString());
            return;
        }
    }
}
/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.examples.helloworld;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HelloWorldClient {
  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  public HelloWorldClient(Channel channel) {
    // Passing Channels to code makes code easier to test and makes it easier to
    // reuse Channels.
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  public void greet(String name) {
    logger.info("Will try to greet " + name + " ...");
    OlaRequest request = OlaRequest.newBuilder().setName(name).build();
    OlaReply response;
    try {
      response = blockingStub.digaOla(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());
  }

  public void greet2(List<String> names) {
    logger.info("Will try to greet " + names + " ...");
    OlasRequest request = OlasRequest.newBuilder().addAllName(names).build();
    OlaReply response;
    try {
      response = blockingStub.digaOlas(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());
  }

  public void greet3(List<String> names) {
    logger.info("Will try to greet " + names + " ...");
    OlasRequest request = OlasRequest.newBuilder().addAllName(names).build();
    OlaReply response;
    try {
      Iterator<OlaReply> it = blockingStub.digaOlas2(request);
      while (it.hasNext()) {
        response = it.next();
        logger.info("Greeting: " + response.getMessage());
      }
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
    }
  }

  public static void main(String[] args) throws Exception {
    String user = "world";
    List<String> users = Arrays.asList("world", "sistemas", "distribuidos", "paulo");
    String target = "localhost:50051";

    if (args.length > 0) {
      if ("--help".equals(args[0])) {
        System.err.println("Usage: [name name name name]");
        System.err.println("");
        System.err.println("  name    The name(s) you wish to be greeted by. Defaults to " + user + " and " + users);
        System.exit(1);
      }
      user = args[0];
      users = Arrays.asList(args);
    }

    // Create a communication channel to the server, known as a Channel.
    // Channels are thread-safe and reusable.
    // It is common to create channels at the beginning of your application and
    // reuse
    // them until the application shuts down.
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS
        // to avoid needing certificates.
        .usePlaintext().build();
    try {
      HelloWorldClient client = new HelloWorldClient(channel);
      logger.info("First Greeting");
      client.greet(user);
      logger.info("Second Greeting");
      client.greet2(users);
      logger.info("Third Greeting");
      client.greet3(users);
    } finally {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}

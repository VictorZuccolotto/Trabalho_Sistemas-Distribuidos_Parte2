package br.ufu.facom.gbc074.projeto.ratis;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.apache.ratis.util.LifeCycle;

public class Servidor {

  // Parametros: myId
  public static void main(String args[]) throws IOException, InterruptedException {
	System.out.println("Inciando servidor...");
    String raftGroupId = ""; // 16 caracteres.

    
    
    
    // Setup for node all nodes.
    Map<String, InetSocketAddress> id2addr = new HashMap<>();
    if(args[1].equals("0")) {
        id2addr.put("p1", new InetSocketAddress("127.0.0.1", 3000));
        id2addr.put("p2", new InetSocketAddress("127.0.0.1", 3200));
        id2addr.put("p3", new InetSocketAddress("127.0.0.1", 3400));
        raftGroupId = "raft_group____um";
    }else if(args[1].equals("1")) {
        id2addr.put("p1", new InetSocketAddress("127.0.0.1", 3600));
        id2addr.put("p2", new InetSocketAddress("127.0.0.1", 3800));
        id2addr.put("p3", new InetSocketAddress("127.0.0.1", 4000));
        raftGroupId = "raft_group__dois";
    }else {
    	System.out.println("O cluster deve ser 0 ou 1");
    	System.exit(0);
    }
    
    
    List<RaftPeer> addresses =
        id2addr.entrySet().stream()
            .map(e -> RaftPeer.newBuilder().setId(e.getKey()).setAddress(e.getValue()).build())
            .collect(Collectors.toList());

    RaftPeerId myId = RaftPeerId.valueOf("p"+args[0]);

    if (addresses.stream().noneMatch(p -> p.getId().equals(myId))) {
      System.out.println("Identificador " + args[0] + " é inválido.");
      System.exit(1);
    }

    // Setup for this node.~/eclipse-workspace/Ratis/src/main/resources
    final int port = id2addr.get("p"+args[0]).getPort();
    RaftProperties properties = new RaftProperties();
    properties.setInt(GrpcConfigKeys.OutputStream.RETRY_TIMES_KEY, Integer.MAX_VALUE);
    GrpcConfigKeys.Server.setPort(properties, port);
    RaftServerConfigKeys.setStorageDir(
        properties, Collections.singletonList(new File("src\\main\\resources\\cluster"+args[1]+"\\" + myId)));

    // Join the group of processes.
    final RaftGroup raftGroup =
        RaftGroup.valueOf(RaftGroupId.valueOf(ByteString.copyFromUtf8(raftGroupId)), addresses);
    RaftServer raftServer =
        RaftServer.newBuilder()
            .setServerId(myId)
            .setStateMachine(new MaquinaDeEstados(args[0]+"-"+args[1]))
            .setProperties(properties)
            .setGroup(raftGroup)
            .build();
    raftServer.start();
    System.out.println("Servidor "+args[0]+ " inciado no cluster "+ args[1]);
    while (raftServer.getLifeCycleState() != LifeCycle.State.CLOSED) {
      TimeUnit.SECONDS.sleep(1);
    }
  }
}

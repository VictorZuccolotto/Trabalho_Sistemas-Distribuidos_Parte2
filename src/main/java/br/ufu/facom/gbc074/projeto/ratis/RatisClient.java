package br.ufu.facom.gbc074.projeto.ratis;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcFactory;
import org.apache.ratis.protocol.ClientId;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;

public class RatisClient {

    String raftGroupIdCluster0 = "raft_group____um"; // 16 caracteres.
    String raftGroupIdCluster1 = "raft_group__dois"; // 16 caracteres.
    
    public List<RaftClient> clusters = new ArrayList<RaftClient>();
    
    public RatisClient() {
    	Map<String, InetSocketAddress> id2addrCluster0 = new HashMap<>();
    	id2addrCluster0.put("p1", new InetSocketAddress("127.0.0.1", 3000));
    	id2addrCluster0.put("p2", new InetSocketAddress("127.0.0.1", 3200));
    	id2addrCluster0.put("p3", new InetSocketAddress("127.0.0.1", 3400));
    	
    	Map<String, InetSocketAddress> id2addrCluster1 = new HashMap<>();
    	id2addrCluster1.put("p1", new InetSocketAddress("127.0.0.1", 3600));
    	id2addrCluster1.put("p2", new InetSocketAddress("127.0.0.1", 3800));
    	id2addrCluster1.put("p3", new InetSocketAddress("127.0.0.1", 4000));
    	
        List<RaftPeer> addressesCluster0 =
        		id2addrCluster0.entrySet().stream()
                    .map(e -> RaftPeer.newBuilder().setId(e.getKey()).setAddress(e.getValue()).build())
                    .collect(Collectors.toList());
        List<RaftPeer> addressesCluster1 =
        		id2addrCluster1.entrySet().stream()
        		.map(e -> RaftPeer.newBuilder().setId(e.getKey()).setAddress(e.getValue()).build())
        		.collect(Collectors.toList());
        
        final RaftGroup raftGroupCluster0 =
                RaftGroup.valueOf(RaftGroupId.valueOf(ByteString.copyFromUtf8(raftGroupIdCluster0)), addressesCluster0);
        
        final RaftGroup raftGroupCluster1 =
        		RaftGroup.valueOf(RaftGroupId.valueOf(ByteString.copyFromUtf8(raftGroupIdCluster1)), addressesCluster1);
            
        RaftProperties raftProperties = new RaftProperties();
    
            RaftClient cluster0 =
                    RaftClient.newBuilder()
                        .setProperties(raftProperties)
                        .setRaftGroup(raftGroupCluster0)
                        .setClientRpc(
                            new GrpcFactory(new Parameters())
                                .newRaftClientRpc(ClientId.randomId(), raftProperties))
                        .build();
            RaftClient cluster1 =
            		RaftClient.newBuilder()
            		.setProperties(raftProperties)
            		.setRaftGroup(raftGroupCluster1)
            		.setClientRpc(
            				new GrpcFactory(new Parameters())
            				.newRaftClientRpc(ClientId.randomId(), raftProperties))
            		.build();
            this.clusters.add(cluster0);
            this.clusters.add(cluster1);
    }
}

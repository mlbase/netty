package discard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import dto.UserPosition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.bson.Document;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class JsonHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String json = "";
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            json = byteBuf.toString(CharsetUtil.UTF_8);
            json = json.trim();
            // Process the JSON string here
            // ...
        }

        ObjectMapper mapper = new ObjectMapper();
        UserPosition userPosition = mapper.readValue(json, UserPosition.class);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
                                .readTimeout(5000, TimeUnit.MILLISECONDS))
                .build();

        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> mongoCollection = database.getCollection("testcollection");
        Document doc = new Document("user_id", userPosition.getUserId())
                .append("position_xy", userPosition.getPositionXY());
        mongoCollection.insertOne(doc);
        UserPosition inserted_data = (UserPosition) mongoCollection.find();
        System.out.println(inserted_data.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

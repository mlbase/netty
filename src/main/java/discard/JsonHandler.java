package discard;
import static com.mongodb.client.model.Filters.eq;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.*;
import org.bson.conversions.Bson;
import dto.UserPosition;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.bson.Document;
import utils.SubscribeHelper;
import utils.SubscribeHelper.*;

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
        MongoCollection<Document> collection = database.getCollection("testcollection");
        Document doc = new Document()
                .append("user_id", userPosition.getUserId())
                .append("position_xy", userPosition.getPositionXY());

        collection.insertOne(doc).subscribe(new ObservableSubscriber<InsertOneResult>() {
        });


        collection.find(eq("user_id", userPosition.getUserId()))
                .subscribe(new PrintDocumentSubscriber());


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

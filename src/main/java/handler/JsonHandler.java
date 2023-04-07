package handler;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.*;
import decoder.JsonDecoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.bson.Document;
import subscriber.InsertSubcriber;


import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class JsonHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channel in");
        if (msg instanceof Document){
            Document document = (Document) msg;
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
            collection.insertOne(document).subscribe(new InsertSubcriber<InsertOneResult>());
        }



    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

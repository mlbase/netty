package decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.bson.Document;

import java.nio.charset.Charset;
import java.util.List;


public class JsonDecoder extends ByteToMessageDecoder {
    private ByteBuf buffer = Unpooled.buffer();
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("decoder in");
        String jsonString = decodeByteBufToString(in);

        // JSON 문자열을 Document 클래스의 인스턴스로 변환
        Document document = Document.parse(jsonString);

        // 변환된 문서를 리스트에 추가
        out.add(document);
    }

    private String decodeByteBufToString(ByteBuf in) {
        if (buffer.readableBytes() < 4) {
            return null;
        }

        int length = buffer.getInt(buffer.readerIndex());
        if (buffer.readableBytes() < length + 4) {
            return null;
        }

        buffer.skipBytes(4);
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, Charset.forName("UTF-8"));
    }
}

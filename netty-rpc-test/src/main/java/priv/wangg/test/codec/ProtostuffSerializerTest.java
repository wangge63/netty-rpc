package priv.wangg.test.codec;

import priv.wangg.rpc.codec.protostuff.ProtostuffSerializer;
import priv.wangg.test.pojo.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ProtostuffSerializerTest {

    public static void main(String[] args) throws IOException {
       User user = new User();
       user.setAge(1);
       user.setName("wangg");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(user);
        os.flush();
        os.close();
        byte[] b = bos.toByteArray();
        System.out.println("JDK serilizable length is " + b.length);

        ProtostuffSerializer serializer = new ProtostuffSerializer();
        byte[] bytes = serializer.serialize(user);
       System.out.println("Protostuff serilizable length is " + bytes.length);
       User user1 = serializer.deserialize(bytes, User.class);
       System.out.println(user1);
    }
}

package com.boeckerman.jake.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import java.util.List;

public class Client {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        Person person = Person.newBuilder().build();
        System.out.println(JsonFormat
                .printer()
                .print(Person
                        .newBuilder()
                        .setEmail("example@example.com")
                        .setId(23423)
                ));

        Person2 foo = Person2.newBuilder().setName("23").setId(4322).addDescription("This is a description").build();

        Person2.Builder bar = Person2.newBuilder();
        bar.setId(23);
        bar.setName("23433");
        System.out.println(bar.getId());

        byte[] serialized = serialize(bar);
        System.out.println(serialized);
//        Person2 built = Person2.newBuilder().mergeFrom(serialized)
//                .setShortCutForCOmplexSubFieldStillUsingKryo(Kryo.serialize(thin.getComplexSubfield()))
//                .build();
//        System.out.println(built);

        List<String> descriptionList = foo.getDescriptionList();
        System.out.println(descriptionList);
    }

    private static byte[] serialize(Person2.Builder bar) {
        return bar.build().toByteArray();
    }
}

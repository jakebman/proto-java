package com.boeckerman.jake.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

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

    }
}

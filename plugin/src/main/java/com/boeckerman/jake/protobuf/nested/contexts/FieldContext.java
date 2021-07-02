package com.boeckerman.jake.protobuf.nested.contexts;

import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions.NullableOptions;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;


public interface FieldContext extends MessageContext {
    FieldDescriptorProto getFieldDescriptorProto();

    NullableOptions getNullableOptions();

    @Override
    MessageContext delegate();

    @Override
    default DescriptorProto getMessageDescriptorProto() {
        return delegate().getMessageDescriptorProto();
    }

    @Override
    default JavaExtensionOptions getMessageExtensions() {
        return delegate().getMessageExtensions();
    }
}

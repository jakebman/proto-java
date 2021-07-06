package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.CodeGenerator;
import com.boeckerman.jake.protobuf.nested.contexts.RootContext;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Design philosophy:
 * The problem is about performing actions at various depths of a nested data structure with no parent reference
 * Those parent references are necessary to provide context:
 * * The name of a file to modify is based on both the FileDescriptorProto and the {Message}DescriptorProto
 * * applying an extension to a message should be able to govern all the fields below it
 * <p>
 * So we use the natural structure of non-static nested classes where an instance of the inner class can
 * reference the fields of the outer instance (https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html#:~:text=Non%2Dstatic%20nested%20classes%20(inner,members%20of%20the%20enclosing%20class.)
 * <p>
 * An alternative solution is to create some form of nested 'Context' object that has a similar structure to these
 * nestings, which does have the benefit of reducing the nesting we see here.
 */

public class RootModifications implements CodeGenerator, NestedStreamingIterable<File>, RootContext {
    final CodeGeneratorRequest request;
    private InvocationParameters invocationParameters;

    public RootModifications(CodeGeneratorRequest request) {
        this.request = request;
        this.invocationParameters = new InvocationParameters(request.getParameter());
    }

    public static Function<String, FileDescriptorProto> fileNameToProtoFileDescriptorLookup(List<FileDescriptorProto> protoFileList) {
        Map<String, FileDescriptorProto> lookup = protoFileList.stream()
                .collect(Collectors.toMap(FileDescriptorProto::getName, Function.identity()));
        return lookup::get;
    }

    @Override // CodeGenerator
    public CodeGeneratorResponse generate() {
        return CodeGeneratorResponse.newBuilder()
                .addAllFile(/*(Iterable<File>)*/ this) // NestedStreamingIterable implements Iterable :D
                .setSupportedFeatures(CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL_VALUE) // anticipate emphatic support - we want this
                .build();
    }

    @Override // NestedStreamable
    public Stream<NestedStreamingIterable<File>> children() {
        return request
                .getFileToGenerateList()
                .stream() // Stream<String> is useless, but we can translate it:
                .map(fileNameToProtoFileDescriptorLookup(this.request.getProtoFileList()))
                .map(this::generateChild);
    }

    private FileDescriptorModifications generateChild(FileDescriptorProto fileDescriptorProto) {
        return new FileDescriptorModifications(this, fileDescriptorProto);
    }

    // Context-passing code to help FieldDescriptorModifications read all necessary context
    @Override
    public CodeGeneratorRequest getCodeGeneratorRequest() {
        return request;
    }

    @Override
    public InvocationParameters getInvocationParameters() {
        return invocationParameters;
    }
}

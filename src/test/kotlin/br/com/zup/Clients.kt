package br.com.zup

import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import javax.inject.Singleton

@Factory
class Clients {

    @Singleton
    fun novaChavePixBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            PixKeyManagerGrpcServiceGrpc.PixKeyManagerGrpcServiceBlockingStub? {
        return PixKeyManagerGrpcServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun removeChavePixBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            RemovePixKeyGrpcServiceGrpc.RemovePixKeyGrpcServiceBlockingStub? {
        return RemovePixKeyGrpcServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun detalhaChavePixBlockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            ListaPixKeyGrpcServiceGrpc.ListaPixKeyGrpcServiceBlockingStub {
        return ListaPixKeyGrpcServiceGrpc.newBlockingStub(channel)
    }
}
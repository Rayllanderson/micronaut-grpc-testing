package br.com.zup.edu

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(
    val repository: CarroRepository,
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub
) {

    /**
     * 1. Happy path
     * 2. Quando já existe um carro com a placa
     * 3. quando os dados de entrada são inválidos
     */

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deve adicionar um novo carro`() {

        val request = CarroRequest.newBuilder()
            .setModelo("Gol")
            .setPlaca("NHX-6842")
            .build()

        val response = grpcClient.adicionar(request)

        with(response){
            assertNotNull(this)
            assertNotNull(id)
            assertTrue(repository.existsById(id))
        }
    }

    @Test
    fun `nao deve adicionar novo carro quando placa ja existe`() {
        val carroExistente = repository.save(Carro(modelo = "Gol", placa = "NGX-3214"))

        val request = CarroRequest.newBuilder()
            .setModelo("Lamborghini")
            .setPlaca(carroExistente.placa)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(request)
        }

        with(error){
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("A placa já está cadastrada", this.status.description)
        }
    }

    @Test
    fun `nao deve adicionar novo carro quando dados de entrada forem invalidos`() {
        val request = CarroRequest.newBuilder()
            .setModelo("")
            .setPlaca("")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(request)
        }

        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Um ou mais campos estão inválidos", this.status.description)
        }
    }

}

@Factory
class Clients {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub {
        return CarrosGrpcServiceGrpc.newBlockingStub(channel)
    }
}
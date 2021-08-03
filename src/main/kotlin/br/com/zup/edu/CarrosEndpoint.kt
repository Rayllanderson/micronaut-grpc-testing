package br.com.zup.edu

import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(
    private val repository: CarroRepository
): CarrosGrpcServiceGrpc.CarrosGrpcServiceImplBase() {

    override fun adicionar(request: CarroRequest?, responseObserver: StreamObserver<CarroResponse>?) {

        val placaJaCadastrada = request?.placa?.let { repository.existsByPlaca(it) } == true
        if(placaJaCadastrada) {
            responseObserver?.onError(Status.ALREADY_EXISTS
                .withDescription("A placa já está cadastrada")
                .asRuntimeException()
            )
            return
        }

        val carro = request?.let {
            Carro(modelo = it.modelo, placa = it.placa)
        }

        try {
            repository.save(carro)
        } catch (e: ConstraintViolationException) {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Um ou mais campos estão inválidos")
                .asRuntimeException()
            )
            return
        }

        val response = CarroResponse.newBuilder().setId(carro?.id!!).build()
        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}
package com.example.proyecto2.service;

import com.example.proyecto2.dto.cliente.ClienteRequest;
import com.example.proyecto2.dto.cliente.ClienteResponse;
import com.example.proyecto2.entidades.Cliente;
import com.example.proyecto2.entidades.Estados.EstadoRegistro;
import com.example.proyecto2.exception.ConflictoNegocioException;
import com.example.proyecto2.exception.RecursoNoEncontradoException;
import com.example.proyecto2.mapper.ClienteMapper;
import com.example.proyecto2.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ClienteService {
    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "id", "nombre", "email", "telefono", "fechaRegistro", "estado"
    );

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(
            ClienteRepository clienteRepository,
            ClienteMapper clienteMapper
    ) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictoNegocioException(
                    "Ya existe un cliente con ese correo electrónico"
            );
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        return clienteMapper.toResponse(clienteGuardado);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = obtenerCliente(id);
        return clienteMapper.toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {
        Ordenamiento.validar(pageable, CAMPOS_ORDENABLES);

        return clienteRepository.findAll(pageable)
                .map(clienteMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> buscarPorNombre(
            String nombre,
            Pageable pageable
    ) {
        Ordenamiento.validar(pageable, CAMPOS_ORDENABLES);

        return clienteRepository
                .findByNombreContainingIgnoreCase(nombre, pageable)
                .map(clienteMapper::toResponse);
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = obtenerCliente(id);

        boolean emailUsadoPorOtroCliente =
                clienteRepository.existsByEmailIgnoreCase(request.getEmail())
                        && !cliente.getEmail().equalsIgnoreCase(request.getEmail());

        if (emailUsadoPorOtroCliente) {
            throw new ConflictoNegocioException(
                    "El correo electrónico ya pertenece a otro cliente"
            );
        }

        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());

        Cliente clienteActualizado = clienteRepository.save(cliente);

        return clienteMapper.toResponse(clienteActualizado);
    }

    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = obtenerCliente(id);
        cliente.setEstado(EstadoRegistro.INACTIVO);
        clienteRepository.save(cliente);
    }

    private Cliente obtenerCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe un cliente con el ID " + id
                        )
                );
    }
}

package com.vluevano.service;

import com.vluevano.model.Cliente;
import com.vluevano.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Consulta todos los clientes
     * @return
     */
    public List<Cliente> consultarClientes() {
        return clienteRepository.findAll();
    }

    /**
     * Busca clientes por filtro
     * @param filtro
     * @return
     */
    public List<Cliente> buscarClientes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return consultarClientes();
        }
        return clienteRepository.buscarPorFiltro(filtro);
    }

    /**
     * Guarda o actualiza un cliente con validaciones
     * @param cliente
     * @return
     */
    @Transactional
    public String guardarCliente(Cliente cliente) {
        // Validaciones básicas
        if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
            return "El nombre del cliente es obligatorio.";
        }

        if (cliente.getRfcCliente() != null && !cliente.getRfcCliente().isEmpty()) {
            if (!cliente.getRfcCliente().matches("[A-Za-z0-9]{12,13}")) {
                return "El RFC debe contener 12 o 13 caracteres alfanuméricos.";
            }
        }

        if (cliente.getTelefonoCliente() != null && !cliente.getTelefonoCliente().isEmpty()) {
            if (!cliente.getTelefonoCliente().matches("\\d{10}")) {
                return "El teléfono debe tener exactamente 10 dígitos.";
            }
        }

        if (cliente.getCorreoCliente() != null && !cliente.getCorreoCliente().isEmpty()) {
            if (!cliente.getCorreoCliente().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return "El correo electrónico no es válido.";
            }
        }

        if (String.valueOf(cliente.getCpCliente()).length() != 5) {
            return "El código postal debe tener 5 dígitos.";
        }

        try {
            clienteRepository.save(cliente);
            return "Cliente guardado exitosamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al guardar: " + e.getMessage();
        }
    }

    /**
     * Elimina un cliente
     * @param cliente
     * @return
     */
    @Transactional
    public boolean eliminarCliente(Cliente cliente) {
        try {
            clienteRepository.delete(cliente);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
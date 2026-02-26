package com.vluevano.service;

import com.vluevano.model.Cliente;
import com.vluevano.repository.ClienteRepository;
import com.vluevano.util.GestorIdioma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private GestorIdioma idioma; 

    /**
     * Consulta todos los clientes registrados en la base de datos
     * @return
     */
    public List<Cliente> consultarClientes() {
        return clienteRepository.findAllActivos();
    }

    /**
     * Busca clientes por un filtro que puede ser parte del nombre, RFC, teléfono o correo electrónico
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
     * Guarda un nuevo cliente o actualiza uno existente. Realiza validaciones básicas antes de guardar
     * @param cliente
     * @return
     */
    @Transactional
    public String guardarCliente(Cliente cliente) {
        if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
            return idioma.get("srv.client.val.name");
        }

        if (cliente.getRfcCliente() != null && !cliente.getRfcCliente().isEmpty()) {
            if (!cliente.getRfcCliente().matches("[A-Za-z0-9]{12,13}")) {
                return idioma.get("srv.client.val.rfc");
            }
        }

        if (cliente.getTelefonoCliente() != null && !cliente.getTelefonoCliente().isEmpty()) {
            if (!cliente.getTelefonoCliente().matches("\\d{10}")) {
                return idioma.get("srv.client.val.phone");
            }
        }

        if (cliente.getCorreoCliente() != null && !cliente.getCorreoCliente().isEmpty()) {
            if (!cliente.getCorreoCliente().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return idioma.get("srv.client.val.email");
            }
        }

        if (String.valueOf(cliente.getCpCliente()).length() != 5) {
            return idioma.get("srv.client.val.cp");
        }

        try {
            clienteRepository.save(cliente);
            return idioma.get("srv.client.msg.success");
        } catch (Exception e) {
            e.printStackTrace();
            return idioma.get("srv.client.msg.error") + " " + e.getMessage();
        }
    }

    /**
     * Elimina un cliente de la base de datos. Retorna true si la eliminación fue exitosa, false en caso contrario
     * @param cliente
     * @return
     */
    @Transactional
    public boolean eliminarCliente(Cliente cliente) {
        try {
            cliente.setActivo(false);
            clienteRepository.save(cliente);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
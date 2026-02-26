package com.vluevano.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoFabricanteId implements Serializable {
    private Integer idProducto;
    private Integer idFabricante;
}
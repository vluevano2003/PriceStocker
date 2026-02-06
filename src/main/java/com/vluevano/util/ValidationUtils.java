package com.vluevano.util;

import javafx.scene.control.TextField;

public class ValidationUtils {

    private static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final String TELEFONO_PATTERN = "^\\d{10}$";
    private static final String RFC_PATTERN = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$";
    private static final String CURP_PATTERN = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]{2}$";

    /**
     * Valida si un TextField está vacío.
     * @param tf
     * @return
     */
    public static boolean esVacio(TextField tf) {
        return tf == null || tf.getText() == null || tf.getText().trim().isEmpty();
    }

    /**
     * Valida email.
     * @param email
     * @return
     */
    public static boolean esEmailValido(String email) {
        return email != null && email.matches(EMAIL_PATTERN);
    }

    /**
     * Valida teléfono (10 dígitos).
     * @param telefono
     * @return
     */
    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null) return false;
        String limpio = telefono.replace(" ", "").replace("-", "");
        return limpio.matches(TELEFONO_PATTERN);
    }

    /**
     * Valida RFC.
     * @param rfc
     * @return
     */
    public static boolean esRfcValido(String rfc) {
        return rfc != null && rfc.toUpperCase().matches(RFC_PATTERN);
    }

    /**
     * Valida CURP.
     * @param curp
     * @return
     */
    public static boolean esCurpValido(String curp) {
        return curp != null && curp.toUpperCase().matches(CURP_PATTERN);
    }
}
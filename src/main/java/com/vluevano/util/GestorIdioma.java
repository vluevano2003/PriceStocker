package com.vluevano.util;

import com.vluevano.model.Configuracion;
import com.vluevano.repository.ConfiguracionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class GestorIdioma {
    
    private ResourceBundle bundle;
    private Locale localeActual;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    /**
     * Al iniciar el sistema, intenta cargar el idioma guardado en la base de datos. Si no existe, establece español por defecto y lo guarda para futuras sesiones
     */
    @PostConstruct
    public void init() {
        Optional<Configuracion> configIdioma = configuracionRepository.findById("IDIOMA_SISTEMA");
        
        if (configIdioma.isPresent()) {
            aplicarIdiomaLocal(new Locale(configIdioma.get().getValor()));
        } else {
            setIdioma(new Locale("es")); 
        }
    }

    /**
     * Establece el idioma del sistema, actualizando el ResourceBundle para reflejar los cambios inmediatamente y guardando la configuración en la base de datos para que se mantenga en futuras sesiones
     * @param locale
     */
    public void setIdioma(Locale locale) {
        aplicarIdiomaLocal(locale);
        Configuracion conf = new Configuracion("IDIOMA_SISTEMA", locale.getLanguage());
        configuracionRepository.save(conf);
    }

    /**
     * Aplica el idioma local actualizando el ResourceBundle con el nuevo locale, lo que permite que las traducciones se reflejen inmediatamente en la interfaz de usuario sin necesidad de reiniciar la aplicación
     * @param locale
     */
    private void aplicarIdiomaLocal(Locale locale) {
        this.localeActual = locale;
        this.bundle = ResourceBundle.getBundle("i18n/messages", locale);
    }

    /**
     * Obtiene el locale actual que se está utilizando en el sistema, lo que permite a otras partes de la aplicación acceder a esta información para mostrar contenido traducido correctamente o realizar otras operaciones relacionadas con el idioma
     * @return
     */
    public Locale getLocaleActual() {
        return localeActual;
    }

    /**
     * Obtiene la traducción correspondiente a la clave proporcionada desde el ResourceBundle, devolviendo un mensaje de error formateado si la clave no se encuentra, lo que permite manejar de manera elegante los casos en los que falten traducciones sin causar errores en la aplicación
     * @param key
     * @return
     */
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    /**
     * Obtiene la traducción correspondiente a la clave proporcionada y formatea el mensaje con los argumentos adicionales, lo que permite incluir valores dinámicos en las traducciones para mostrar información personalizada o contextualizada en la interfaz de usuario de manera clara y legible
     * @param key
     * @param args
     * @return
     */
    public String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
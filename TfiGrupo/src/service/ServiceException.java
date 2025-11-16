package service;

// Excepción personalizada para propagar errores de negocio
// (Validaciones, IDs no encontrados) a la capa de Menú.
public class ServiceException extends Exception {
    
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

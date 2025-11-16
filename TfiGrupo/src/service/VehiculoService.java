package service;

import entities.Vehiculo;
import java.util.List;

public interface VehiculoService {
    
    void insertar(Vehiculo vehiculo) throws ServiceException;
    
    void actualizar(Vehiculo vehiculo) throws ServiceException;
    
    void eliminar(long id) throws ServiceException;
    
    Vehiculo getById(long id) throws ServiceException;
    
    List<Vehiculo> getAll() throws ServiceException;
    
    // Requerimiento específico del TFI (búsqueda por campo relevante)
    Vehiculo getByDominio(String dominio) throws ServiceException;
}
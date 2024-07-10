package com.espinoza.infraccionservice.soap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.espinoza.infraccionservice.entity.AddInfraccionRequest;
import com.espinoza.infraccionservice.entity.AddInfraccionResponse;
import com.espinoza.infraccionservice.entity.DeleteInfraccionRequest;
import com.espinoza.infraccionservice.entity.DeleteInfraccionResponse;
import com.espinoza.infraccionservice.entity.GetAllInfraccionesRequest;
import com.espinoza.infraccionservice.entity.GetAllInfraccionesResponse;
import com.espinoza.infraccionservice.entity.GetInfraccionRequest;
import com.espinoza.infraccionservice.entity.GetInfraccionResponse;
import com.espinoza.infraccionservice.entity.InfraccionDetalle;
import com.espinoza.infraccionservice.entity.ServiceStatus;
import com.espinoza.infraccionservice.entity.Infraccion;
import com.espinoza.infraccionservice.entity.UpdateInfraccionRequest;
import com.espinoza.infraccionservice.entity.UpdateInfraccionResponse;
import com.espinoza.infraccionservice.service.InfraccionService;

@Endpoint
public class InfraccionEndPoint {
    private static final String NAMESPACE_URI = "http://espinoza.com/infraccionservice";

    @Autowired
    private InfraccionService service;
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAllInfraccionesRequest")
    @ResponsePayload
    public GetAllInfraccionesResponse findAll(@RequestPayload GetAllInfraccionesRequest request) {
        GetAllInfraccionesResponse response = new GetAllInfraccionesResponse();
        
        Pageable page = PageRequest.of(request.getOffset(), request.getLimit());
        List<Infraccion> infracciones;
        if (request.getTexto() == null) {
            infracciones = service.findAll(page);
        } else {
            infracciones = service.findByDni(request.getTexto(), page);
        }
        
        List<InfraccionDetalle> infraccionesResponse = new ArrayList<>();
        for (Infraccion infraccion : infracciones) {
            InfraccionDetalle detalle = new InfraccionDetalle();
            BeanUtils.copyProperties(infraccion, detalle);
            infraccionesResponse.add(detalle);
        }
        response.getInfraccionDetalle().addAll(infraccionesResponse);
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetInfraccionRequest")
    @ResponsePayload
    public GetInfraccionResponse findById(@RequestPayload GetInfraccionRequest request) {
        GetInfraccionResponse response = new GetInfraccionResponse();
        Infraccion infraccion = service.findById(request.getId());
        if (infraccion != null) {
            InfraccionDetalle detalle = new InfraccionDetalle();
            BeanUtils.copyProperties(infraccion, detalle);
            response.setInfraccionDetalle(detalle);
        }
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "AddInfraccionRequest")
    @ResponsePayload
    public AddInfraccionResponse create(@RequestPayload AddInfraccionRequest request) {
        AddInfraccionResponse response = new AddInfraccionResponse();
        ServiceStatus serviceStatus = new ServiceStatus();
        
        Infraccion infraccion = new Infraccion();
        infraccion.setDni(request.getDni());
        infraccion.setTipoInfraccion(request.getTipoInfraccion());
        infraccion.setUbicacion(request.getUbicacion());
        infraccion.setDescripcion(request.getDescripcion());
        infraccion.setMontoMulta(request.getMontoMulta());
        infraccion.setEstado(request.getEstado());
        
        Infraccion savedInfraccion = service.save(infraccion);
        
        if (savedInfraccion != null) {
            InfraccionDetalle detalle = new InfraccionDetalle();
            BeanUtils.copyProperties(savedInfraccion, detalle);
            response.setInfraccionDetalle(detalle);
            serviceStatus.setStatusCode("SUCCESS");
            serviceStatus.setMessage("Infraccion added successfully.");
        } else {
            serviceStatus.setStatusCode("CONFLICT");
            serviceStatus.setMessage("Infraccion already exists or could not be added.");
        }
        
        response.setServiceStatus(serviceStatus);
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "UpdateInfraccionRequest")
    @ResponsePayload
    public UpdateInfraccionResponse update(@RequestPayload UpdateInfraccionRequest request) {
        UpdateInfraccionResponse response = new UpdateInfraccionResponse();
        ServiceStatus serviceStatus = new ServiceStatus();
        
        Infraccion infraccion = service.findById(request.getId());
        if (infraccion != null) {
            infraccion.setDni(request.getDni());
            infraccion.setTipoInfraccion(request.getTipoInfraccion());
            infraccion.setUbicacion(request.getUbicacion());
            infraccion.setDescripcion(request.getDescripcion());
            infraccion.setMontoMulta(request.getMontoMulta());
            infraccion.setEstado(request.getEstado());
            
            Infraccion updatedInfraccion = service.update(infraccion);
            if (updatedInfraccion != null) {
                InfraccionDetalle detalle = new InfraccionDetalle();
                BeanUtils.copyProperties(updatedInfraccion, detalle);
                response.setInfraccionDetalle(detalle);
                serviceStatus.setStatusCode("SUCCESS");
                serviceStatus.setMessage("Infraccion updated successfully.");
            } else {
                serviceStatus.setStatusCode("CONFLICT");
                serviceStatus.setMessage("Infraccion could not be updated.");
            }
        } else {
            serviceStatus.setStatusCode("NOT_FOUND");
            serviceStatus.setMessage("Infraccion not found.");
        }
        
        response.setServiceStatus(serviceStatus);
        return response;
    }
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteInfraccionRequest")
    @ResponsePayload
    public DeleteInfraccionResponse delete(@RequestPayload DeleteInfraccionRequest request) {
        DeleteInfraccionResponse response = new DeleteInfraccionResponse();
        ServiceStatus serviceStatus = new ServiceStatus();
        
        boolean deleted = service.delete(request.getId());
        if (deleted) {
            serviceStatus.setStatusCode("SUCCESS");
            serviceStatus.setMessage("Infraccion deleted successfully.");
        } else {
            serviceStatus.setStatusCode("CONFLICT");
            serviceStatus.setMessage("Infraccion could not be deleted.");
        }
        
        response.setServiceStatus(serviceStatus);
        return response;
    }
}

package eu.reportincident.incident_service.service.impl;

import eu.reportincident.incident_service.event.IncidentCreatedEvent;
import eu.reportincident.incident_service.event.RabbitMQProducer;
import eu.reportincident.incident_service.exception.NotFoundException;
import eu.reportincident.incident_service.model.dto.Incident;
import eu.reportincident.incident_service.model.entity.IncidentEntity;
import eu.reportincident.incident_service.model.entity.IncidentImageEntity;
import eu.reportincident.incident_service.model.enums.IncidentStatus;
import eu.reportincident.incident_service.model.request.FilterRequest;
import eu.reportincident.incident_service.model.request.IncidentRequest;
import eu.reportincident.incident_service.model.request.IncidentStatusUpdateRequest;
import eu.reportincident.incident_service.repository.IncidentEntityRepository;
import eu.reportincident.incident_service.service.IncidentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentEntityRepository incidentRepository;
    private final ModelMapper modelMapper;
    private final RabbitMQProducer rabbitMQProducer;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public IncidentServiceImpl(IncidentEntityRepository incidentRepository, ModelMapper modelMapper, RabbitMQProducer rabbitMQProducer) {
        this.incidentRepository = incidentRepository;
        this.modelMapper = modelMapper;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @Override
    public Incident findById(long id) {
        // TODO: Anonymous users get only APPROVED incidents, ADMIN can get any status
        return modelMapper.map(incidentRepository.findById(id).orElseThrow(NotFoundException::new), Incident.class);
    }

    @Override
    public Incident create(IncidentRequest incidentRequest) {
        IncidentEntity incidentEntity = modelMapper.map(incidentRequest, IncidentEntity.class);
        incidentEntity.setReportedAt(LocalDateTime.now());

        List<IncidentImageEntity> images = new ArrayList<>(incidentEntity.getImages());
        for (IncidentImageEntity imageEntity : images) {
            imageEntity.setIncident(incidentEntity);
        }

        incidentEntity.setImages(images);
        incidentEntity = incidentRepository.saveAndFlush(incidentEntity);

        // Send event that new incident has been created
        IncidentCreatedEvent event = new IncidentCreatedEvent(incidentEntity.getId(), LocalDateTime.now());
        rabbitMQProducer.sendIncidentCreatedEvent(event);

        return modelMapper.map(incidentEntity, Incident.class);
    }

    @Override
    public Page<Incident> filter(Pageable page, FilterRequest filterRequest) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<IncidentEntity> query = cb.createQuery(IncidentEntity.class);
        Root<IncidentEntity> root = query.from(IncidentEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtriranje prema incidentType
        if (filterRequest.getIncidentType() != null) {
            predicates.add(cb.equal(root.get("type"), filterRequest.getIncidentType()));
        }

        // Filtriranje prema incidentSubtype
        if (filterRequest.getIncidentSubtype() != null) {
            predicates.add(cb.equal(root.get("subtype"), filterRequest.getIncidentSubtype()));
        }

        // Filtriranje prema location
        // TODO: Filter on given parameters, not only city name
        if (filterRequest.getLocation() != null) {
            predicates.add(cb.equal(root.get("location").get("city"), filterRequest.getLocation()));
        }

        // Filtriranje prema approved
        if (filterRequest.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filterRequest.getStatus()));
        }

        // Filtriranje prema vremenskom periodu (ako je timeRange postavljen)
        if (filterRequest.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("reportedAt"), filterRequest.getStartDate()));
        }

        query.select(root).where(predicates.toArray(new Predicate[0]));

        TypedQuery<IncidentEntity> typedQuery = entityManager.createQuery(query);

        // Paginacija
        long totalElements = typedQuery.getResultList().size();
        typedQuery.setFirstResult((int) page.getOffset());
        typedQuery.setMaxResults(page.getPageSize());

        List<IncidentEntity> incidentEntities = typedQuery.getResultList();
        List<Incident> incidents = incidentEntities.stream()
                .map(e -> modelMapper.map(e, Incident.class))
                .collect(Collectors.toList());

        return new PageImpl<>(incidents, page, totalElements);
    }

    @Override
    public Page<Incident> findAll(Pageable page) {

        Page<IncidentEntity> incidentEntityPage = incidentRepository.findAll(page);

        List<Incident> incidents = incidentEntityPage.stream()
                .map(incidentEntity -> modelMapper.map(incidentEntity, Incident.class))
                .collect(Collectors.toList());

        return new PageImpl<>(incidents, page, incidentEntityPage.getTotalElements());
    }

    @Override
    public Page<Incident> findByStatus(Pageable page, IncidentStatus status) {
        Page<IncidentEntity> incidentEntityPage = incidentRepository.findByStatus(status, page);

        List<Incident> incidents = incidentEntityPage.stream()
                .map(incidentEntity -> modelMapper.map(incidentEntity, Incident.class))
                .collect(Collectors.toList());

        return new PageImpl<>(incidents, page, incidentEntityPage.getTotalElements());
    }

    @Override
    public Incident updateStatus(long id, IncidentStatusUpdateRequest status) {
        Optional<IncidentEntity> incidentEntity = incidentRepository.findById(id);
        if (incidentEntity.isPresent()) {
            IncidentEntity updatedIncident = incidentEntity.get();
            updatedIncident.setStatus(status.getStatus());
            updatedIncident = incidentRepository.saveAndFlush(updatedIncident);
            return modelMapper.map(updatedIncident, Incident.class);
        } else {
            throw new NotFoundException("Incident not found with id: " + id);
        }
    }
}

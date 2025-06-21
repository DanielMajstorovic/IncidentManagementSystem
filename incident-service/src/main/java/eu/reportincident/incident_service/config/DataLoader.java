package eu.reportincident.incident_service.config;

import eu.reportincident.incident_service.model.entity.IncidentEntity;
import eu.reportincident.incident_service.model.entity.IncidentImageEntity;
import eu.reportincident.incident_service.model.dto.Location;
import eu.reportincident.incident_service.model.enums.IncidentStatus;
import eu.reportincident.incident_service.model.enums.IncidentSubtype;
import eu.reportincident.incident_service.model.enums.IncidentType;
import eu.reportincident.incident_service.repository.IncidentEntityRepository;
import eu.reportincident.incident_service.repository.IncidentImageEntityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(IncidentEntityRepository incidentRepository, IncidentImageEntityRepository imageRepository) {
        return args -> {
            if (incidentRepository.count() == 0) {
                for (int i = 0; i < 50; i++) {
                    List<IncidentImageEntity> images = generateIncidentImages(i);
                    IncidentEntity incident = IncidentEntity.builder()
                            .type(IncidentType.values()[i % IncidentType.values().length])
                            .subtype(i % 2 == 0 ? IncidentSubtype.values()[i % IncidentSubtype.values().length] : null)
                            .description("Test incident description " + i)
                            .location(generateLocation(i))
                            .reportedAt(LocalDateTime.now())
                            .status(i % 2 == 0 ? IncidentStatus.APPROVED : IncidentStatus.PENDING)
                            .build();

                    for (IncidentImageEntity image : images) {
                        image.setIncident(incident);
                    }

                    incident.setImages(images);

                    incidentRepository.save(incident);
                }
            }
        };
    }

    private Location generateLocation(int index) {
        return switch (index % 5) {
            case 0 ->
                    new Location(43.8486, 18.3564, 1000.0, "Maršala Tita 8", "Sarajevo", "Federacija Bosne i Hercegovine", "Bosna i Hercegovina", "71000");
            case 1 ->
                    new Location(43.3433, 17.8073, 1000.0, "Braće Fejića 21", "Mostar", "Federacija Bosne i Hercegovine", "Bosna i Hercegovina", "88000");
            case 2 ->
                    new Location(44.7721, 17.1916, 1000.0, "Kralja Petra I Karađorđevića 32", "Banja Luka", "Republika Srpska", "Bosna i Hercegovina", "78000");
            case 3 ->
                    new Location(44.2055, 17.9029, 1000.0, "Zmaja od Bosne 64", "Zenica", "Federacija Bosne i Hercegovine", "Bosna i Hercegovina", "72000");
            default ->
                    new Location(44.5376, 18.6724, 1000.0, "Armije BiH 45", "Tuzla", "Federacija Bosne i Hercegovine", "Bosna i Hercegovina", "75000");
        };
    }

    private List<IncidentImageEntity> generateIncidentImages(int index) {
        String imageUrl = "https://via.placeholder.com/150?text=Image" + index;
        IncidentImageEntity imageEntity = new IncidentImageEntity();
        imageEntity.setImageUrl(imageUrl);
        return List.of(imageEntity);
    }
}

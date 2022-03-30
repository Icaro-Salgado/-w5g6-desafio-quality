package br.com.mercadolivre.desafioquality.services.validators;

import br.com.mercadolivre.desafioquality.exceptions.DatabaseManagementException;
import br.com.mercadolivre.desafioquality.exceptions.DatabaseReadException;
import br.com.mercadolivre.desafioquality.models.Neighborhood;
import br.com.mercadolivre.desafioquality.repository.ApplicationRepository;
import br.com.mercadolivre.desafioquality.services.exceptions.NeighborhoodNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NeighborhoodValidationService {
    private final ApplicationRepository<Neighborhood, UUID> neighborhoodRepository;

    public void validate(String neighborhoodName) throws DatabaseReadException, DatabaseManagementException, NeighborhoodNotFoundException {
       this.validateExistence(neighborhoodName);
    }

    private void validateExistence(String neighborhoodName) throws DatabaseReadException, DatabaseManagementException, NeighborhoodNotFoundException {
        Optional<Neighborhood> foundedNeighborhood = neighborhoodRepository
                .read()
                .stream()
                .filter(n -> n.getNameDistrict().equals(neighborhoodName))
                .findFirst();

        if(foundedNeighborhood.isEmpty()) {
            throw new NeighborhoodNotFoundException("Bairro não encontrado");
        }
    }
}

package br.com.mercadolivre.desafioquality.services;

import br.com.mercadolivre.desafioquality.exceptions.DatabaseReadException;
import br.com.mercadolivre.desafioquality.exceptions.DatabaseWriteException;
import br.com.mercadolivre.desafioquality.exceptions.DbEntryAlreadyExists;
import br.com.mercadolivre.desafioquality.exceptions.NeighborhoodNotFoundException;
import br.com.mercadolivre.desafioquality.models.Neighborhood;
import br.com.mercadolivre.desafioquality.repository.NeighborhoodRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class NeighborhoodService {

    private final NeighborhoodRepository neighborhoodRepository;


    public Neighborhood createNeighborhood(Neighborhood newNeighborhood) throws DbEntryAlreadyExists, DatabaseWriteException, DatabaseReadException {

        List<Neighborhood> neighborhoods = neighborhoodRepository.read();

        Optional<Neighborhood> existingNeighborhood = neighborhoods
                .stream()
                .filter(neighborhood -> neighborhood.getNameDistrict().equals(newNeighborhood.getNameDistrict()))
                .findFirst();

        if (existingNeighborhood.isPresent()) {
            throw new DbEntryAlreadyExists(newNeighborhood
                    .getNameDistrict()
                    .concat(" já está cadastrado na base de dados")
            );
        }

        newNeighborhood.setId(UUID.randomUUID());
        return neighborhoodRepository.add(newNeighborhood);
    }

    public List<Neighborhood> listNeighborhood(Integer page, Integer limit) throws DatabaseReadException {
        if(page == null || page < 0 || limit == null || limit < 0){
            throw new InvalidParameterException("Limite ou página inválida");
        }

        Integer offSet =  page <= 1 ? 0 : (page - 1)  * limit;

        return neighborhoodRepository.read(offSet, limit);
    }

    public Neighborhood getNeighborhoodById(UUID id) throws DatabaseReadException {
        Optional<Neighborhood> neighborhoodExists = this.neighborhoodRepository.find(id);
        if (neighborhoodExists.isPresent()) {
            return neighborhoodExists.get();
        }
        throw new NeighborhoodNotFoundException("Bairro não encontrado");
    }


    public void deleteNeighborhoodById(){}

    public Integer getTotalPages(Integer limit) throws DatabaseReadException {
        if (limit == null || limit <= 0) {
            return 0;
        }

        int results = neighborhoodRepository.read().size();

        return new BigDecimal(results).divide(new BigDecimal(limit), RoundingMode.CEILING).intValue();
    }

    public void deleteNeighborhoodById(UUID id) throws DatabaseReadException, DatabaseWriteException {
        Neighborhood neighborhood = getNeighborhoodById(id);
        this.neighborhoodRepository.delete(neighborhood);
    }
}

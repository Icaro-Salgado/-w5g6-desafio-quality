package br.com.mercadolivre.desafioquality.integration;

import br.com.mercadolivre.desafioquality.exceptions.DbEntryAlreadyExists;
import br.com.mercadolivre.desafioquality.models.Neighborhood;
import br.com.mercadolivre.desafioquality.test_utils.DatabaseUtils;
import br.com.mercadolivre.desafioquality.test_utils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(profiles = "test")
public class NeighborhoodIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatabaseUtils<Neighborhood[]> databaseUtils;

    private final String filename = "neighborhood.json";

    @BeforeAll
    public void beforeAll() {
        databaseUtils.startDatabase(filename);
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        databaseUtils.loadDefaultFiles(filename, Neighborhood[].class);
    }

    @AfterAll
    public void afterAll(){
        databaseUtils.deleteDatabase();
    }

    @Test
    @DisplayName("NeighborhoodController - GET - /api/v1/neighborhood/{neighborhoodId}")
    public void testFindANeighborHoodById() throws Exception {
        List<Neighborhood> neighborhoods = new ArrayList<>();
        Neighborhood neighborhood = new Neighborhood(UUID.fromString("77b3737d-7450-4d94-8f95-936e2c17e2cc"), "São Paulo", BigDecimal.valueOf(2000.0));
        neighborhoods.add(neighborhood);

        databaseUtils.writeIntoFile(filename, neighborhoods);

        mockMvc.perform(MockMvcRequestBuilders.
                        get("/api/v1/neighborhood/{neighborhoodId}", "77b3737d-7450-4d94-8f95-936e2c17e2cc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nameDistrict").value("São Paulo"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.valueDistrictM2").value(2000.0));
    }

    @Test
    @DisplayName("NeighborhoodController - DELETE - /api/v1/neighborhood/{neighborhoodId}")
    public void testDeleteANeighborHoodById() throws Exception {
        List<Neighborhood> neighborhoods = new ArrayList<>();
        Neighborhood neighborhood = new Neighborhood(UUID.fromString("77b3737d-7450-4d94-8f95-936e2c17e2cc"), "São Paulo", BigDecimal.valueOf(2000.0));
        neighborhoods.add(neighborhood);

        databaseUtils.writeIntoFile(filename, neighborhoods);

        mockMvc.perform(MockMvcRequestBuilders.
                        delete("/api/v1/neighborhood/{neighborhoodId}", "77b3737d-7450-4d94-8f95-936e2c17e2cc"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());


        Neighborhood[] updatedNeighborhoods = databaseUtils.readFromFile(filename, Neighborhood[].class);
        Assertions.assertEquals(0, updatedNeighborhoods.length);
    }


    @Test
    @DisplayName("NeighborhoodController - GET - /api/v1/neighborhood/")
    public void testNeighborhoodList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/neighborhood/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.neighborhoods").isNotEmpty());
    }

    @Test
    @DisplayName("NeighborhoodController - GET - /api/v1/neighborhood/?size=1")
    public void testNeighborhoodListWithMultiplePages() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/neighborhood/?size=1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.neighborhoods.length()").value(1));
    }

    @Test
    @DisplayName("NeighborhoodController - GET - /api/v1/neighborhood/?page=2&size=4")
    public void testNeighborhoodListInAnotherPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/neighborhood/?page=2&size=4"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.neighborhoods.length()").value(1));
    }

    @Test
    @DisplayName("NeighborhoodController - GET - /api/v1/neighborhood/?page=2&size=null")
    public void testNeighborhoodListWhenReceiveInvalidParameter() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/neighborhood/?page=2&size=null"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("NeighborhoodController - POST - /api/v1/neighborhood/")
    public void testPostNeighborhood() throws Exception {

        Neighborhood neighborhood = Neighborhood.builder()
                .nameDistrict("Vila Olímpia")
                .valueDistrictM2(BigDecimal.valueOf(45000))
                .build();

        ObjectMapper Obj = new ObjectMapper();

        String payload = Obj.writeValueAsString(neighborhood);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/neighborhood/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(MockMvcResultMatchers.status().isCreated());

    }

    @Test
    @DisplayName("NeighborhoodController - POST - /api/v1/neighborhood/ - valueDistrictM2 negativo")
    public void testNegativeValueDistrictM2InPostNeighborhood() throws Exception {

        Neighborhood neighborhood = Neighborhood.builder()
                .nameDistrict("Bairro Fake")
                .valueDistrictM2(BigDecimal.valueOf(-1))
                .build();

        TestUtils.assertErrorMessage(
                neighborhood,
                "/api/v1/neighborhood/",
                "O valor do metro quadrado do bairro não pode ser menor ou igual a zero!",
                mockMvc);
    }

    @Test
    @DisplayName("NeighborhoodController - POST - /api/v1/neighborhood/ - valueDistrictM2 com mais de 13 digitos")
    public void testValueDistrictM2Over13DigitsInPostNeighborhood() throws Exception {

        Neighborhood neighborhood = Neighborhood.builder()
                .nameDistrict("Bairro Fake")
                .valueDistrictM2(BigDecimal.valueOf(12345678901234.0))
                .build();

        TestUtils.assertErrorMessage(
                neighborhood,
                "/api/v1/neighborhood/",
                "O valor do metro quadrado não pode exceder 13 digitos!",
                mockMvc);
    }

    @Test
    @DisplayName("NeighborhoodController - POST - /api/v1/neighborhood/ - valueDistrictM2 nulo")
    public void testNullValueDistrictM2InPostNeighborhood() throws Exception {

        Neighborhood neighborhood = Neighborhood.builder()
                .nameDistrict("Bairro Fake")
                .valueDistrictM2(null)
                .build();

        TestUtils.assertErrorMessage(
                neighborhood,
                "/api/v1/neighborhood/",
                "O valor do metro quadrado do bairro não pode ficar vazio!",
                mockMvc);
    }

    @Test
    @DisplayName("NeighborhoodController - POST - /api/v1/neighborhood/ - nome do bairro vazio")
    public void testEmptyDistrictNameInPostNeighborhood() throws Exception {

        Neighborhood neighborhood = Neighborhood.builder()
                .nameDistrict("")
                .valueDistrictM2(BigDecimal.valueOf(10000.0))
                .build();

        TestUtils.assertErrorMessage(
                neighborhood,
                "/api/v1/neighborhood/",
                "O bairro não pode ficar vazio!",
                mockMvc);
    }

    @Test
    @DisplayName("NeighborhoodController - POST - /api/v1/neighborhood/ - nome excedendo limite")
    public void testDistrictNameOverLimitInPostNeighborhood() throws Exception {

        Neighborhood neighborhood = Neighborhood.builder()
                .nameDistrict("Fake Neighborhoodddddddddddddddddddddddddddddd")
                .valueDistrictM2(BigDecimal.valueOf(10000.0))
                .build();

        TestUtils.assertErrorMessage(
                neighborhood,
                "/api/v1/neighborhood/",
                "O comprimento do bairro não pode exceder 45 caracteres!",
                mockMvc);
    }

    @Test
    @DisplayName("NeighborhoodController - POST - /api/v1/neighborhood/ - testando adição de bairro já existente")
    public void testDistrictAlreayAddedInPostNeighborhood() throws Exception {

        List<Neighborhood> neighborhoods = new ArrayList<>();
        Neighborhood neighborhood = new Neighborhood(UUID.randomUUID(), "O Clone", BigDecimal.valueOf(20000.0));
        neighborhoods.add(neighborhood);

        databaseUtils.writeIntoFile(filename, neighborhoods);

        ObjectMapper Obj = new ObjectMapper();

        String payload = Obj.writeValueAsString(neighborhood);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/neighborhood/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andReturn();

        Optional<DbEntryAlreadyExists> someException = Optional.ofNullable((DbEntryAlreadyExists) result.getResolvedException());
        if (someException.isPresent()) {
            String msg = someException.get().getMessage();
            Assertions.assertEquals(msg, neighborhood.getNameDistrict().concat(" já está cadastrado na base de dados"));
        }
    }
}
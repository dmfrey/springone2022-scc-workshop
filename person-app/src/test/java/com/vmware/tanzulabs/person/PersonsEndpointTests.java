package com.vmware.tanzulabs.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( PersonsEndpoint.class )
public class PersonsEndpointTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PersonService mockPersonService;

    private UUID fakePersonId = UUID.randomUUID();
    private String fakeFirstName = "first_name";
    private String fakeLastName = "last_name";
    private String fakeCustomerId = UUID.randomUUID().toString();
    private UUID fakeOrderId = UUID.randomUUID();

    @Test
    void testPersons() throws Exception {

        var fakePerson = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.of( List.of( new Order( fakeOrderId, fakeCustomerId ) ) ) );
        when( this.mockPersonService.getPersons() ).thenReturn( List.of( fakePerson ) );

        this.mockMvc.perform( get( "/persons/" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$[0].id" ).value( fakePersonId.toString() ) )
                .andExpect( jsonPath( "$[0].firstName" ).value( fakeFirstName ) )
                .andExpect( jsonPath( "$[0].lastName" ).value( fakeLastName ) )
                .andExpect( jsonPath( "$[0].customerId" ).value( fakeCustomerId ) )
                .andExpect( jsonPath( "$[0].orders[0].id" ).value( fakeOrderId.toString() ) )
                .andExpect( jsonPath( "$[0].orders[0].customerId" ).value( fakeCustomerId ) );

        verify( this.mockPersonService ).getPersons();
        verifyNoMoreInteractions( this.mockPersonService );

    }

    @Test
    void testPerson() throws Exception {

        var fakePerson = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.of( List.of( new Order( fakeOrderId, fakeCustomerId ) ) ) );
        when( this.mockPersonService.getPersonById( fakePersonId ) ).thenReturn( fakePerson );

        this.mockMvc.perform( get( "/persons/{id}", fakePersonId.toString() ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( fakePersonId.toString() ) )
                .andExpect( jsonPath( "$.firstName" ).value( fakeFirstName ) )
                .andExpect( jsonPath( "$.lastName" ).value( fakeLastName ) )
                .andExpect( jsonPath( "$.customerId" ).value( fakeCustomerId ) )
                .andExpect( jsonPath( "$.orders[0].id" ).value( fakeOrderId.toString() ) )
                .andExpect( jsonPath( "$.orders[0].customerId" ).value( fakeCustomerId ) );

        verify( this.mockPersonService ).getPersonById( fakePersonId );
        verifyNoMoreInteractions( this.mockPersonService );

    }

}

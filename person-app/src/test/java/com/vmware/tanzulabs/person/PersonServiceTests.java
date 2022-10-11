package com.vmware.tanzulabs.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest( classes = PersonService.class )
public class PersonServiceTests {

    @Autowired
    PersonService subject;

    @MockBean
    PersonRepository mockPersonRepository;

    // We can only test what we think the Orders API is returning to us
    @MockBean
    OrderResourceService mockOrderResourceService;

    private UUID fakePersonId = UUID.randomUUID();
    private String fakeFirstName = "first_name";
    private String fakeLastName = "last_name";
    private String fakeCustomerId = UUID.randomUUID().toString();
    private UUID fakeOrderId = UUID.randomUUID();

    @Test
    void testGetPersons() {

        var fakePerson = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.empty() );
        when( this.mockPersonRepository.allPersons() ).thenReturn( List.of( fakePerson ) );

        var fakeOrder = new Order( fakeOrderId, fakeCustomerId );
        when( this.mockOrderResourceService.lookupOrdersByCustomerId( fakeCustomerId ) ).thenReturn( Optional.of( List.of( fakeOrder ) ) );

        var actual = this.subject.getPersons();

        var expected = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.of( List.of( new Order( fakeOrderId, fakeCustomerId ) ) ) );

        assertThat( actual )
                .isNotEmpty()
                .hasSize( 1 )
                .containsExactly( expected );

        verify( this.mockPersonRepository ).allPersons();
        verify( this.mockOrderResourceService ).lookupOrdersByCustomerId( fakeCustomerId );
        verifyNoMoreInteractions( this.mockPersonRepository, this.mockOrderResourceService );

    }

    @Test
    void testGetPersonById() {

        var fakePerson = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.empty() );
        when( this.mockPersonRepository.personById( fakePersonId) ).thenReturn( fakePerson );

        var fakeOrder = new Order( fakeOrderId, fakeCustomerId );
        when( this.mockOrderResourceService.lookupOrdersByCustomerId( fakeCustomerId ) ).thenReturn( Optional.of( List.of( fakeOrder ) ) );

        var actual = this.subject.getPersonById( fakePersonId );

        var expected = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.of( List.of( new Order( fakeOrderId, fakeCustomerId ) ) ) );

        assertThat( actual ).isEqualTo( expected );

        verify( this.mockPersonRepository ).personById( fakePersonId );
        verify( this.mockOrderResourceService ).lookupOrdersByCustomerId( fakeCustomerId );
        verifyNoMoreInteractions( this.mockPersonRepository, this.mockOrderResourceService );

    }

}

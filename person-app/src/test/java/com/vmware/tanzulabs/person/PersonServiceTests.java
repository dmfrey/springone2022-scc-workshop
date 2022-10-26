package com.vmware.tanzulabs.person;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;

import static com.vmware.tanzulabs.person.SpringCloudContractHelper.repoRoot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest( classes = { OrderResourceConfiguration.class, PersonService.class, OrderResourceService.class },
        properties = {
            "webservice.order-resource-url=http://localhost:8099"
        }
)
public class PersonServiceTests {
    static Map<String, String> contractProperties() {

        Map<String, String> map = new HashMap<>();
        map.put( "stubs.find-producer", "true" );

        return map;
    }

    @RegisterExtension
    static StubRunnerExtension stubRunnerExtension = new StubRunnerExtension()
            .downloadStub("com.vmware.tanzu-labs", "order-app" ).withPort( 8099 )
            .repoRoot( repoRoot() )
            .stubsMode( StubRunnerProperties.StubsMode.REMOTE )
            .withMappingsOutputFolder( "target/outputmappings" )
            .withProperties( contractProperties() );

    @Autowired
    PersonService subject;

    @MockBean
    PersonRepository mockPersonRepository;

    // We can only test what we think the Orders API is returning to us

    private UUID fakePersonId = UUID.randomUUID();
    private String fakeFirstName = "first_name";
    private String fakeLastName = "last_name";
    private String fakeCustomerId = "1";
    private UUID fakeOrderId = UUID.fromString("f803c8fc-38ee-4949-8829-2c03d364d3ac");

    @Test
    void testGetPersons() {

        var fakePerson = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.empty() );
        when( this.mockPersonRepository.allPersons() ).thenReturn( List.of( fakePerson ) );

        var actual = this.subject.getPersons();

        var expected = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.of( List.of( new Order( fakeOrderId, fakeCustomerId ) ) ) );

        assertThat( actual )
                .isNotEmpty()
                .hasSize( 1 )
                .containsExactly( expected );

        verify( this.mockPersonRepository ).allPersons();
        verifyNoMoreInteractions( this.mockPersonRepository );

    }

//    @Test
//    void testGetPersonById() {
//
//        var fakePerson = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.empty() );
//        when( this.mockPersonRepository.personById( fakePersonId) ).thenReturn( fakePerson );
//
//        var fakeOrder = new Order( fakeOrderId, fakeCustomerId );
//
//        var actual = this.subject.getPersonById( fakePersonId );
//
//        var expected = new Person( fakePersonId, fakeFirstName, fakeLastName, fakeCustomerId, Optional.of( List.of( new Order( fakeOrderId, fakeCustomerId ) ) ) );
//
//        assertThat( actual ).isEqualTo( expected );
//
//        verify( this.mockPersonRepository ).personById( fakePersonId );
//        verifyNoMoreInteractions( this.mockPersonRepository );
//
//    }

}

final class SpringCloudContractHelper {

    public static String repoRoot() {

        try {

            var projectModulePath = FileSystems.getDefault().getPath("" ).toAbsolutePath();
            var rootPath = projectModulePath.getParent().toAbsolutePath();
            var stubsPath = Paths.get( rootPath.toString(), "/stubs" );

            return "stubs://file://" + stubsPath;

        } catch( Exception e ) {

            System.err.println( e );
            e.printStackTrace();

            return "";
        }

    }

}

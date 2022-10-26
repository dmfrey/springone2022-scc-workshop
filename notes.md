## Setting up the contract tests

#### Add SCC Verifier

Add this to your order-app pom.xml file dependencies section
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-contract-verifier</artifactId>
            <scope>test</scope>
        </dependency>
```

#### Add SCC plugin to order-app

Add this to your order-app pom.xml file build plugins section

```xml
            <plugin>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-contract-maven-plugin</artifactId>
                <extensions>true</extensions>
                <configuration>
                    <testFramework>JUNIT5</testFramework>
                    <baseClassMappings>
                        <baseClassMapping>
                            <contractPackageRegex>.*orders-by-customer-id.*</contractPackageRegex>
                            <baseClassFQN>com.vmware.tanzulabs.order.OrdersByCustomerIdBase</baseClassFQN>
                        </baseClassMapping>
                    </baseClassMappings>
                    <outputDirectory>${maven.multiModuleProjectDirectory}/stubs</outputDirectory>
                    <stubsDirectory>${maven.multiModuleProjectDirectory}/stubs</stubsDirectory>
                </configuration>
            </plugin>
```

#### Write contract

Create the contracts directory in the order-app: src/test/resources/contracts
Make the first contract directory as well: src/test/resources/contracts/orders-by-customer-id

Create a file called orders-by-customer-id-request.yml and paste in the following:
```yaml
request:
  method: GET
  url: /orders/1
response:
  status: 200
  body:
    - orderId: "f803c8fc-38ee-4949-8829-2c03d364d3ac"
      customerId: 1
  headers:
    Content-Type: application/json
```

#### Set up base class that runs the contract


```java
package com.vmware.tanzulabs.order;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrdersByCustomerIdBase {

    OrderRepository mockOrderRepository = mock( OrderRepository.class );

    private final String fakeCustomerId = "1";
    private final String fakeCustomerIdNotFound = "2";
    private final UUID fakeOrderId = UUID.fromString( "f803c8fc-38ee-4949-8829-2c03d364d3ac" );

    @BeforeEach
    void setup() {

        when( this.mockOrderRepository.ordersByCustomerId( fakeCustomerId ) )
                .thenReturn( List.of( new Order( fakeOrderId, fakeCustomerId ) ) );

        when( this.mockOrderRepository.ordersByCustomerId( fakeCustomerIdNotFound ) )
                .thenReturn( Collections.emptyList() );

        RestAssuredMockMvc.standaloneSetup( new OrdersEndpoint( this.mockOrderRepository ) );

    }

}

```

#### Verify contract and delete the old test
Run from Maven side menu: order app - plugins - spring cloud contract plugin contract:generateTests and then run from generated test
This test run should have generated a .json file in your stubs directory. This is what the consumer will use to check its own requests against.

#### Run consumer side tests
Go to the PersonServiceTests in the person-app subproject and run them. Now we'll need to set them up to use the stub runner.
Copy the following into your person app pom.xml file:

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
            <scope>test</scope>
        </dependency>

Copy the following into your PersonServiceTests:

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

Copy the following into the very bottom of the same tests:

    final class SpringCloudContractHelper {
    
        public static String repoRoot() {
    
            try {
    
                var projectModulePath = FileSystems.getDefault().getPath("" ).toAbsolutePath();
                var projectParentPath = projectModulePath.getParent().toAbsolutePath();
                var rootPath = projectParentPath.getParent().toAbsolutePath();
                var stubsPath = Paths.get( rootPath.toString(), "/stubs" );
    
                return "stubs://file://" + stubsPath;
    
            } catch( Exception e ) {
    
                System.err.println( e );
                e.printStackTrace();
    
                return "";
            }
    
        }
    
    }

Add the following to your Spring boot config for this test file:

    properties = {
        "webservice.order-resource-url=http://localhost:8099"
    }

Add the OrderResourceService to your Spring boot config:

    @SpringBootTest( classes = { PersonService.class, OrderResourceService.class },
    properties = {
    "webservice.order-resource-url=http://localhost:8099"
    }
    )

Delete the OrderResourceService mock bean, as well as any references:

    @MockBean
    OrderResourceService mockOrderResourceService;

Your test should look like this when you're done:

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

Add this to your configuration in this test file as well:
    OrderResourceConfiguration.class,

Replace the customer ID and order ID to match what the contract is giving us data-wise:

    private String fakeCustomerId = "1";
    private UUID fakeOrderId = UUID.fromString("f803c8fc-38ee-4949-8829-2c03d364d3ac");

I had to change my paths to find the stubs, ended up with:
    var projectModulePath = FileSystems.getDefault().getPath("" ).toAbsolutePath();
    var rootPath = projectModulePath.getParent().toAbsolutePath();
    var stubsPath = Paths.get( rootPath.toString(), "/stubs" );
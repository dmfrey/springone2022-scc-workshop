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
Run from Maven side menu: order app - plugins - spring cloud contract plugin contract:generateTests and then run from test


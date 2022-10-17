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

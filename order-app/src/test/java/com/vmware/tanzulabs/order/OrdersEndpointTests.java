package com.vmware.tanzulabs.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( OrdersEndpoint.class )
public class OrdersEndpointTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderRepository mockOrderRepository;

    @Test
    void testGetOrdersByCustomerId() throws Exception {

        var fakeOrderId = UUID.randomUUID();
        var fakeCustomerId = UUID.randomUUID().toString();
        when( this.mockOrderRepository.ordersByCustomerId( fakeCustomerId ) ).thenReturn( List.of( new Order( fakeOrderId, fakeCustomerId ) ) );

        this.mockMvc.perform( get( "/orders/{customerId}", fakeCustomerId ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$[0].orderId" ).value( fakeOrderId.toString() ) )
                .andExpect( jsonPath( "$[0].customerId" ).value( fakeCustomerId ) );

        verify( this.mockOrderRepository ).ordersByCustomerId( fakeCustomerId );
        verifyNoMoreInteractions( this.mockOrderRepository );

    }

}

package uk.gov.companieshouse.reconciliation.function.compare_count;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@CamelSpringBootTest
@SpringBootTest
@DirtiesContext
@TestPropertySource(locations = "classpath:application-stubbed.properties")
public class CompareCountRouteTest {

    @Autowired
    private CamelContext context;

    @Produce("direct:compare_count")
    private ProducerTemplate template;

    @EndpointInject("mock:a")
    private MockEndpoint mockCorporateBodyCountEndpoint;

    @EndpointInject("mock:b")
    private MockEndpoint mockCompanyProfileCountEndpoint;

    @EndpointInject("mock:result")
    private MockEndpoint mockResult;

    @AfterEach
    void after() {
        mockCorporateBodyCountEndpoint.reset();
        mockCompanyProfileCountEndpoint.reset();
        mockResult.reset();
    }

    @Test
    void testEndpointAIsGreaterThanEndpointB() throws InterruptedException {
        mockCorporateBodyCountEndpoint.returnReplyBody(ExpressionBuilder.constantExpression(BigDecimal.valueOf(15)));
        mockCompanyProfileCountEndpoint.returnReplyBody(ExpressionBuilder.constantExpression(10L));
        mockResult.allMessages().body().isEqualTo("A has 5 more things than B");
        template.sendBodyAndHeaders(0, createHeaders());
        MockEndpoint.assertIsSatisfied(context);
    }

    @Test
    void testEndpointBIsGreaterThanEndpointA() throws InterruptedException {
        mockCorporateBodyCountEndpoint.returnReplyBody(ExpressionBuilder.constantExpression(BigDecimal.valueOf(10)));
        mockCompanyProfileCountEndpoint.returnReplyBody(ExpressionBuilder.constantExpression(20L));
        mockResult.allMessages().body().isEqualTo("B has 10 more things than A");
        template.sendBodyAndHeaders(0, createHeaders());
        MockEndpoint.assertIsSatisfied(context);
    }

    @Test
    void testEndpointsAreTheSame() throws InterruptedException {
        mockCorporateBodyCountEndpoint.returnReplyBody(ExpressionBuilder.constantExpression(BigDecimal.valueOf(10)));
        mockCompanyProfileCountEndpoint.returnReplyBody(ExpressionBuilder.constantExpression(10));
        mockResult.allMessages().body().isEqualTo("A and B contain the same number of things");
        template.sendBodyAndHeaders(0, createHeaders());
        MockEndpoint.assertIsSatisfied(context);
    }

    private Map<String, Object> createHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Src", "mock:a");
        headers.put("SrcName", "A");
        headers.put("Target", "mock:b");
        headers.put("TargetName", "B");
        headers.put("Comparison", "things");
        headers.put("Destination", "mock:result");
        return headers;
    }
}

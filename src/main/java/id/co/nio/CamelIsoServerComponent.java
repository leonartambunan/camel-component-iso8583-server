package id.co.nio;

import java.util.Map;

import org.apache.camel.Endpoint;

import org.apache.camel.support.DefaultComponent;

@org.apache.camel.spi.annotations.Component("iso8583server")
public class CamelIsoServerComponent extends DefaultComponent {
    
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new CamelIsoServerEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;

    }

}

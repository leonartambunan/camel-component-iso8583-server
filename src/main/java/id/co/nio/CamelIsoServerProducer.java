//package id.co.nio;
//
//import org.apache.camel.Exchange;
//import org.apache.camel.support.DefaultProducer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class CamelIsoServerProducer extends DefaultProducer {
//    private static final Logger LOG = LoggerFactory.getLogger(CamelIsoServerProducer.class);
//    private CamelIsoServerEndpoint endpoint;
//
//    public CamelIsoServerProducer(CamelIsoServerEndpoint endpoint) {
//        super(endpoint);
//        this.endpoint = endpoint;
//    }
//
//    public void process(Exchange exchange) throws Exception {
//        System.out.println(exchange.getIn().getBody());
//    }
//
//}

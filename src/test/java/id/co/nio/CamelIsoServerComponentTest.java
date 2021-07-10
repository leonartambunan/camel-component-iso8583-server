package id.co.nio;

import com.github.kpavlov.jreactive8583.iso.ISO8583Version;
import com.github.kpavlov.jreactive8583.iso.J8583MessageFactory;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CamelIsoServerComponentTest extends CamelTestSupport {

    private final EventBusHelper eventBusHelper = EventBusHelper.getInstance();

    @Test
    public void testisoserver() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(5);

        // Trigger events to subscribers
        simulateEventTrigger();

        mock.await();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("iso8583server:7001")
                  .process(new Processor() {
                      @Override
                      public void process(Exchange exchange) throws Exception {
                          String data = (String) exchange.getIn().getBody();

                          MessageFactory mf = new MessageFactory();
                          ConfigParser.configureFromClasspathConfig(mf, "bank.xml");
                          J8583MessageFactory messageFactory = new J8583MessageFactory<>(mf, ISO8583Version.V1987);// [1]

                          IsoMessage isoMessage = messageFactory.parseMessage(data.getBytes(StandardCharsets.UTF_8),0);

                          IsoMessage response = messageFactory.createResponse(isoMessage);

                          response.setField(39,new IsoValue(IsoType.ALPHA,"00",2));

                          exchange.getMessage().setBody(response.debugString());

                          log.info("HAHAHAHAHAHAHAHAH");

                      }
                  }).end();
            }
        };
    }

    private void simulateEventTrigger() {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                final Date now = new Date();
                // publish events to the event bus
                eventBusHelper.publish(now);
            }
        };

        new Timer().scheduleAtFixedRate(task, 1000L, 1000L);
    }
}

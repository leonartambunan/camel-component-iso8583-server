# Camel Component - ISO8583 Server

ISO8583 Server as Camel Component

## Libraries
- j8583 (com.solab.iso8583)
- jReactive-8583 (https://github.com/kpavlov/jreactive-8583)
- slf4j

## How to Build

```$ mvn -DskipTests clean package```


## How to Use

### ISO8583 Specification

Put your j8583 xml spec in the root of Java class loader (usually as src/main/resources/iso8583server.xml)

### Code

```java


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

.....

from("iso8583server:7001")
.process(new Processor() {
   @Override
   public void process(Exchange exchange) throws Exception {
         IsoMessage data = (IsoMessage) exchange.getIn().getBody();
         MessageFactory mf = new MessageFactory();
         ConfigParser.configureFromClasspathConfig(mf, "iso8583server.xml");
         J8583MessageFactory messageFactory = new J8583MessageFactory<>(mf, ISO8583Version.V1987);// [1]
         IsoMessage response = messageFactory.createResponse(data);
         //your main process here
         response.setField(39,new IsoValue(IsoType.ALPHA,"00",2));
         exchange.getMessage().setBody(response);
   }
}).end();
```

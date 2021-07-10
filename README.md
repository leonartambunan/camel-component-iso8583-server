# Camel Component - ISO8583 Server

ISO8583 Server as Camel Component

## Libraries
- j8583
- jReactive-8583 https://github.com/kpavlov/jreactive-8583
- slf4j

## How to Build

```$ mvn -DskipTests clean package```


## How to Use

```java
from("iso8583server:7001") //you may change the port 70001 to met your requirement
.process(new Processor() {
         @Override
         public void process(Exchange exchange) throws Exception {
            IsoMessage data = (String) exchange.getIn().getBody();
            ....
         }
}).end();
```

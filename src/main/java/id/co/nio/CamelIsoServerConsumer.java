package id.co.nio;

import com.github.kpavlov.jreactive8583.IsoMessageListener;
import com.github.kpavlov.jreactive8583.iso.ISO8583Version;
import com.github.kpavlov.jreactive8583.iso.J8583MessageFactory;
import com.github.kpavlov.jreactive8583.server.Iso8583Server;
import com.github.kpavlov.jreactive8583.server.ServerConfiguration;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;
import io.netty.channel.ChannelHandlerContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultConsumer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

public class CamelIsoServerConsumer extends DefaultConsumer {
    private final CamelIsoServerEndpoint endpoint;
    private final EventBusHelper eventBusHelper;

    private ExecutorService executorService;

    Logger logger = LoggerFactory.getLogger(CamelIsoServerConsumer.class);

    private Iso8583Server<IsoMessage> server = null;

    public static int port = 7000;

    public CamelIsoServerConsumer(CamelIsoServerEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
        eventBusHelper = EventBusHelper.getInstance();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        logger.info("Starting {} endpoint",endpoint.getEndpointUri());

        String portStr = endpoint.getEndpointUri().substring(16);

        logger.info("Port:{}",portStr);

        port = Integer.parseInt(portStr);

        URL specUrl = getClass().getClassLoader().getResource("iso8583server.xml");

        if (specUrl==null) {
            logger.warn("***************************************************************************");
            logger.error("* iso8583server.xml file is not found. Endpoint will use the default spec *");
            logger.error("***************************************************************************");
        } else {
            logger.info("Specification file : {}",specUrl.toString());
        }

        //Runnable runnable = () -> {

        try {
            MessageFactory mf = new MessageFactory();

            if (specUrl==null) {
                ConfigParser.configureFromClasspathConfig(mf, "bank.xml");
            } else {
                ConfigParser.configureFromUrl(mf, specUrl);
            }

            J8583MessageFactory messageFactory = new J8583MessageFactory<>(mf, ISO8583Version.V1987);// [1]

            ServerConfiguration serverConfiguration = ServerConfiguration.newBuilder()
                    .addLoggingHandler(true)
                    .logSensitiveData(true)
                    .workerThreadsCount(4)
                    .replyOnError(true)
                    .workerThreadsCount(12)
                    .idleTimeout(0)
                    .build();

            server = new Iso8583Server<IsoMessage>(port, serverConfiguration, messageFactory);// [2]

            server.addMessageListener(new IsoMessageListener<IsoMessage>() {

                public boolean onMessage(@NotNull ChannelHandlerContext ctx, @NotNull IsoMessage isoMessage) {
                    Runnable runnable = () -> {
                        try {

                            Exchange exchange = getEndpoint().createExchange();

                            logger.info("-------------------------");
                            logger.info(isoMessage.debugString());
                            logger.info("-------------------------");

                            exchange.getIn().setBody(isoMessage.debugString());

                            getProcessor().process(exchange);

                            Message m = exchange.getMessage();

                            String data = (String) m.getBody();

                            IsoMessage response = messageFactory.parseMessage(data.getBytes(StandardCharsets.UTF_8), 0);

                            logger.info(response.debugString());

                            ctx.writeAndFlush(response);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    };

                    Thread t = new Thread(runnable);
                    t.start();
                    //t.join();
                    return false;
                }

                public boolean applies(@NotNull IsoMessage isoMessage) {
                    return true;
                } // [3]


            });
            serverConfiguration.replyOnError();
            server.init();

            server.start();// [6]
            if (server.isStarted()) { // [7]
                System.out.println("ding dong");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        //};

        //new Thread(runnable).start();

        // start a single threaded pool to monitor events
        // executorService = endpoint.createExecutor();

        // submit task to the thread pool
        /*executorService.submit(() -> {
            // subscribe to an event
            eventBusHelper.subscribe(this::onEventListener);
        });*/
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (server!=null) {
            server.shutdown();
        }
        // shutdown the thread pool gracefully
        getEndpoint().getCamelContext().getExecutorServiceManager().shutdownGraceful(executorService);
    }

    private void onEventListener(final Object event) {
        final Exchange exchange = createExchange(false);

        exchange.getIn().setBody("Hello World! The time is " + event);

        try {
            // send message to next processor in the route
            getProcessor().process(exchange);
        } catch (Exception e) {
            exchange.setException(e);
        } finally {
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
            releaseExchange(exchange, false);
        }
    }
}

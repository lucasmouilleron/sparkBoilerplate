package sparkboilerplate;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class SparkBoilerplate
{

    ////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args)
    {
        disableLog4J();

        Spark.port(8080);

        Spark.get("/hello", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                return "Hello !!!";
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static void disableLog4J()
    {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "OFF");
        List<String> loggers = Collections.<String>list(LogManager.getLogManager().getLoggerNames());
        for(String logger : loggers)
        {
            LogManager.getLogManager().getLogger(logger).setLevel(Level.OFF);
        }
    }
}

package sparkboilerplate;

import ch.qos.logback.classic.Logger;
import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

public class SparkBoilerplate
{

    ////////////////////////////////////////////////////////////////////////////////
    public static final int REST_PORT = 8080;
    public static final String REST_PASSWORD = "ilovekate";
    public static final String JWT_PRIVATE_KEY_FILE = "./jwt/private_key.der";
    public static final String JWT_PUBLIC_KEY_FILE = "./jwt/public_key.der";
    public static final String SSL_KEYSTORE_FILE = "./ssl/keystore";
    public static final String SSL_KEYSTORE_PASSWORD = "password";
    public static final String SSL_TRUSTSTORE_FILE = "./ssl/truststore";
    public static final String SSL_TRUSTSTORE_PASSWORD = "password";
    ////////////////////////////////////////////////////////////////////////////////
    public static PrivateKey JWTPrivateKey;
    public static PublicKey JWTPublicKey;
    public static Token token;

    ////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args)
    {

        try
        {
            disableLog4J();
            JWTPrivateKey = makeJWTPrivateKey();
            JWTPublicKey = makeJWTPublicKey();
            initSpark();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    ////////////////////////////////////////////////////////////////////////////////
    private static void initSpark() throws Exception
    {
        // INIT
        Spark.port(REST_PORT);
        Spark.secure(SSL_KEYSTORE_FILE, SSL_KEYSTORE_PASSWORD, SSL_TRUSTSTORE_FILE, SSL_TRUSTSTORE_PASSWORD);

        // FILTERS
        Filter protectedFiler = new Filter()
        {

            @Override
            public void handle(Request rqst, Response rspns) throws Exception
            {
                boolean authenticated = true;
                String tokenString = rqst.headers("token");
                try
                {
                    token = checkSignObject(tokenString, Token.class);
                    //System.out.println(token.date);
                }
                catch(Exception e)
                {
                    authenticated = false;
                }
                if(!authenticated)
                {
                    Spark.halt(401, "You are not welcome here");
                }
            }
        };
        Spark.before("/protected", protectedFiler);
        Spark.before("/protected/*", protectedFiler);
        Spark.before("/otherProtected", protectedFiler);

        // EXCEPTIONS
        Spark.exception(Exception.class, new ExceptionHandler()
        {

            @Override
            public void handle(Exception excptn, Request rqst, Response rspns)
            {
                excptn.printStackTrace();
                rspns.status(500);
                rspns.body("Somethign wrong happened");
            }
        });

        // ROUTES
        Spark.get("/hello", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                return "Hello !!!";
            }
        });

        Spark.get("/hello/:id", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                return "Hello " + rqst.params(":id") + " !!!";
            }
        });

        Spark.get("/objectToJson", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                return new Person("lucas", "super");
            }
        }, new JsonTransformer());

        Spark.get("/mapToJson", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                HashMap map = new HashMap();
                map.put("name", "lucas");
                map.put("power", "super");
                return map;
            }
        }, new JsonTransformer());

        Spark.get("/redirect", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                //throw new Exception();
                rspns.redirect("/hello");
                return null;
            }
        });

        Spark.get("/login/:password", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                String password = rqst.params(":password");
                if(password.equals(REST_PASSWORD))
                {
                    return signObject(new Token("username", System.currentTimeMillis()));
                }
                else
                {
                    throw new Exception("can't authentify");
                }

            }
        });

        Spark.get("/protected", new Route()
        {
            @Override
            public Object handle(Request rqst, Response rspns) throws Exception
            {
                return "Private !!!";
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static String signObject(Object object) throws Exception
    {
        Gson gson = new Gson();
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(gson.toJson(object));
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKey(JWTPrivateKey);
        return jws.getCompactSerialization();

    }

    ////////////////////////////////////////////////////////////////////////////////
    private static <T> T checkSignObject(String jwtString, Class<T> type) throws Exception
    {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKey(JWTPublicKey);
        jws.setCompactSerialization(jwtString);
        Gson gson = new Gson();
        return gson.fromJson(jws.getPayload(), type);
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static PrivateKey makeJWTPrivateKey() throws Exception
    {
        File f = new File(JWT_PRIVATE_KEY_FILE);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static PublicKey makeJWTPublicKey() throws Exception
    {
        File fPublic = new File("./jwt/public_key.der");
        FileInputStream fisPublic = new FileInputStream(fPublic);
        DataInputStream disPublic = new DataInputStream(fisPublic);
        byte[] keyPublicBytes = new byte[(int) fPublic.length()];
        disPublic.readFully(keyPublicBytes);
        disPublic.close();
        X509EncodedKeySpec specPublic = new X509EncodedKeySpec(keyPublicBytes);
        KeyFactory kfPublic = KeyFactory.getInstance("RSA");
        return kfPublic.generatePublic(specPublic);
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class JsonTransformer implements ResponseTransformer
    {

        private Gson gson = new Gson();

        @Override
        public String render(Object model)
        {
            return gson.toJson(model);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static void disableLog4J()
    {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(ch.qos.logback.classic.Level.ERROR);
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class Person
    {

        public String name;
        public String power;

        public Person(String name, String power)
        {
            this.name = name;
            this.power = power;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static class Token
    {

        public String name;
        public long date;

        public Token(String name, long date)
        {
            this.name = name;
            this.date = date;
        }
    }
}

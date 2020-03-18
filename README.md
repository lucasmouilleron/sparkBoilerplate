sparkBoilerplate
================

A simple Spark REST boilerplate.

![Screenshot](http://grabs.lucasmouilleron.com/Screen%20Shot%202015-11-23%20at%2016.30.13.png)

Install
-------
- Install JDK 8
- Install Apache Ant
- Install Apache Ivy
- Get dependencies : `ant getDependencies`

Run and test
------------
- Run : `ant run`
- `https://localhost:8087/hello`

Authentication
--------------
- JWT
- Generate keys : 
    - Private key : `openssl genrsa -out private_key.pem 2048`
    - Private key java format : `openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt`
    - Public key java format : `openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der`
- Get token : `curl -k https://localhost:8080/login/ilovekate`
- Acces restricted area : `curl -k --header "token: eyJhbGciOiJSUzI1NiJ9.eyJuYW1lIjoidXNlcm5hbWUiLCJkYXRlIjoxNDQzNzgwODU3NDcyfQ.A6b6Hg1QyYgYUV8J9wff6SvclX90Ydmx6sd8OzTAXYJ6gLpkXMBaHvOLtyxu35hSiiVwrOljnfLSg__tlbzh6PG8KrMuezwCQHttnQzowfp5CxNWM5mEXcMjHiCMLGW3X_p5MV6hm7pe7M8aBDKlZj__OHEMPogFGSga5HhnRDnRfzY49cW9CgzdtfCY3c-wWwabyoy75kQTk5GG2KUOZPy5xKT9EJvL1JLlGKkCl4Il8zNGm2cpP68_hIqCohLqEfbMXjBdccYU7DsjQowBxtjQcZD92pu-6rHKhWcJVlNC32BqvPQXn5laKCm9Dpq703Km_IWvEPz0LPXDtIZ9Gg" https://localhost:8080/protected`

CORS
---
- enableCORS()
- Beware in filters, bypass `OPTIONS` requests so it works

SSL
---
- Generate keystore : 
    - `keytool -genkey -keyalg RSA -alias sparkBoilerplate -keystore keystore -validity 3600 -keysize 2048`
    - "What is your first and last name?" has to be the domain name
- Self signing : `keytool -export -alias sparkBoilerplate -file certificate.crt -keystore keystore`
- CA signing : 
    - Generate CSR (certificate request) : `keytool -keystore keystore -certreq -alias sparkBoilerplate -keyalg rsa -file certificate.csr`
    - Get from CA the certificate.crt file
- Generate trustore : `keytool -import -file certificate.crt -alias sparkBoilerplate -keystore truststore`
- Test keystore : `keytool -list -v -keystore keystore`
- In this boilerplate, password is `password` and host is `localhost`
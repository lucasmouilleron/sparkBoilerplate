sparkBoilerplate
================

Authentication
--------------
- JWT
- Generate keys : 
    - Private key : `openssl genrsa -out private_key.pem 2048`
    - Private key java format : `openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt`
    - Public key java format : `openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der`
- Get token : `curl -k https://localhost:8080/login/ilovekate`
- Acces restricted area : `curl -k --header "token: eyJhbGciOiJSUzI1NiJ9.MTQ0Mzc3MzUzNDMzMA.d4XJ_zXYBAXxaM6UYgHUnwEpxPSh2Hv4yp0XVvZwM9PTPHgl0b3Qy8SseK7nVTWehMeHXo5XJrTdxZF0c9BWH_-27wnu3t54Z8zsRZnm4Ies07VXRIbOy-p1U0UzPwX24TqkzQnDX5poIrVrbJcxoqmRCvgBxpx06hqtNja0CU4xH2Ezk1wwSdKdWtQOHVhGvZyAaWYts9S80fyE4wY7i0uDri2vt3mzhN6Z2leXWsZABiVH85V-sNmIhUAU-bclXYWJ-HbFytzSBMpv9t9QaJyFnub-y3MfVMQGudnijd6io_SlbelkWWv2eiu2XSmEZttr0zrsaklD1P-_lLYCvQ" https://localhost:8080/protected`

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
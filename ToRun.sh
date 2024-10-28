rm -rf new_keystore.jks

openssl s_client -showcerts -connect repo.maven.apache.org:443 </dev/null 2>/dev/null | openssl x509 -outform PEM > maven.cer

sudo keytool -importcert -trustcacerts -file maven.cer -alias mavenRepo -keystore new_keystore.jks -storepass abcdef

./gradlew build -Djavax.net.ssl.trustStore=new_keystore.jks -Djavax.net.ssl.trustStorePassword=abcdef
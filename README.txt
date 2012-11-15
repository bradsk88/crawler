To package as JNLP:
Export executable Jar using eclipse, etc.
open command line and navigate to jar directory.
keytool -genkey -keyalg RSA -keystore keystore.ks -alias myalias
<enter passwords>
jarsigner -keystore keystore dbtool.jar myalias
<enter passwords>
all:
	export CLASSPATH=/usr/cs-local/339/xmlrpc-3.1.3/lib/commons-logging-1.1.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/ws-commons-util-1.0.2.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/xmlrpc-client-3.1.3.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/xmlrpc-common-3.1.3.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/xmlrpc-server-3.1.3.jar:/usr/cs-local/339/sqlite-jdbc-3.8.11.2.jar:$CLASSPATH:.

	javac assets/*.java
all:
	# For this program you will need to use Apache xmlrpc. 
	# If you are a Williams College student, then run this in bash:
	# export CLASSPATH=/usr/cs-local/339/xmlrpc-3.1.3/lib/commons-logging-1.1.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/ws-commons-util-1.0.2.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/xmlrpc-client-3.1.3.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/xmlrpc-common-3.1.3.jar:/usr/cs-local/339/xmlrpc-3.1.3/lib/xmlrpc-server-3.1.3.jar:$CLASSPATH
	# Otherwise, download it here:
	# http://ws.apache.org/xmlrpc/
	# We use version 3.1.3.

	javac src/*.java

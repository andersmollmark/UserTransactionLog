###### Special to get ActiveMQ to work on MS ########

// Edit in activemq in /bin
// find location where path/location to pid-file is created
// hardcode the path to /var/run/activemq.pid

// EX BEFORE EDIT
# Location of the pidfile
if [ -z "$ACTIVEMQ_PIDFILE" ]; then
  ACTIVEMQ_PIDFILE="$ACTIVEMQ_DATA/activemq.pid"
fi

// AFTER EDIT
# Location of the pidfile
if [ -z "$ACTIVEMQ_PIDFILE" ]; then
  ACTIVEMQ_PIDFILE="/var/run/activemq.pid"
fi


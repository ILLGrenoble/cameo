
## Dependencies 
 - cmake 3.7.2
 - cppzmq
 - rapidjson
 - doxygen (optional)
 
### CENTOS 8 

List of packages:
 - cmake
 - cppzmq-devel
 - rapidjson-devel
 
```
pkgs="cmake zeromq-devel rapidjson-devel"
yum install -y $pkgs
```

### Debian XXX



### Ubuntu XXX
```sudo apt install cmake libzmq3-dev rapidjson-dev```


## Compilation instructions

```
mkdir build/
cd build/
cmake ..
cmake --build .
```

# To Do
 - [ ] remove zmq deprecated methods
 ```
 /opt/panosc/cameo/src/impl/SubscriberImpl.cpp:210:36: warning: ‘bool zmq::detail::socket_base::recv(zmq::message_t*, int)’ is deprecated: from 4.3.1, use recv taking a reference to message_t and recv_flags [-Wdeprecated-declarations]
    m_subscriber->recv(message.get());
                                    ^
In file included from /opt/panosc/cameo/src/impl/SocketWaitingImpl.h:23,
                 from /opt/panosc/cameo/src/impl/SubscriberImpl.h:20,
                 from /opt/panosc/cameo/src/impl/SubscriberImpl.cpp:17:
/usr/include/zmq.hpp:1267:10: note: declared here
     bool recv(message_t *msg_, int flags_ = 0)
          ^~~~

## Development 
How to find the list of public headers and check that they are all in the include/ directory
 1. move all the headers in src/
 2. move cameo.h to include/
 3. run the following command multiple times until there is no output
```
#!/bin/fish
set -l ORIG_DIR src
for f in (for file in include/*; grep include $file | grep '"'; end | sort | uniq | sed 's|.* "||;s|"||'); if [ -e $ORIG_DIR/$f ] ; echo $f; git mv $ORIG_DIR/$f include/; end; end
```

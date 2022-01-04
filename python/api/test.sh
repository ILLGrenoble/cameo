#!/bin/bash

INSTALL_PREFIX=$1

PATH=${PATH}:/usr/local/bin/
which cameo-server > /dev/null || exit 1


CAMEO_SERVER_DIR=/usr/local/share/java/
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/tmp/devel/lib64 # this should be the CMAKE_INSTALL_PREFIX
# LD_LIBRARY_PATH should include the directory where the libjzmq.so is
# if it is manually installed it is /usr/local/lib
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib:$INSTALL_PREFIX
export PYTHONPATH=$PYTHONPATH:$INSTALL_PREFIX/python3.6/site-packages/

#if [ ! -e ${JAVA_APP} ];then
#    JAVA_APP=`which java`
#fi

# Launch the server
coproc SERVER ( java -jar "${CAMEO_SERVER_DIR}/cameo-server.jar" test/cameo_config.xml &> /dev/null )
#coproc SERVER ( cameo-server test/cameo_config.xml  ) # the kill does not work..... the java app remeais


echo "Cameo server PID: ${SERVER_PID}"

if kill -0 ${SERVER_PID}; then
    # execute the tests as defined in test/CMakeLists.txt
    cd build/testing-prefix/src/testing-build
    # stop-on-failure implemented only in cmake 3.18.5 onward
    ctest -V --output-on-failure --stop-on-failure #-I 1,4,1 # -R requesterPy 
    #ctest --output-on-failure --stop-on-failure -R subscriber #-I 4,4,1 # -R requesterPy 
    kill -9 ${SERVER_PID}
else
    echo "BAD"
    kill  ${SERVER_PID}
    exit 1
fi

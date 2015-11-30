#!/bin/sh
set -e
# check to see if protobuf folder is empty
if [ ! -d "$HOME/thrift-${THRIFT_VERSION}" ]; then
  wget http://www.us.apache.org/dist/thrift/${THRIFT_VERSION}/thrift-${THRIFT_VERSION}.tar.gz
  tar xfz thrift-${THRIFT_VERSION}.tar.gz
  cd thrift-${THRIFT_VERSION} && ./configure --without-ruby --without-erlang --without-go --without-nodejs && sudo make install && cd ..
else
  echo 'Using cached directory.';
fi

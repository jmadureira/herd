namespace java io.herd.thrift.test

service fibonacci {
    i32 getFibonacci(1: required i32 number);
}
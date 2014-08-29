package io.herd.netty.codec.thrift;

import org.apache.thrift.TException;

import io.herd.thrift.test.fibonacci;

public class FibonacciService implements fibonacci.Iface {

    @Override
    public int getFibonacci(int number) throws TException {
        if (number <= 1) {
            return 1;
        }
        return getFibonacci(number - 1) + getFibonacci(number - 2);
    }

}

package io.herd.example.scala

import io.herd.scala.interop._
import io.herd.server.Application
import io.herd.thrift.Thrift
import io.herd.thrift.ThriftModule

trait ScalaExampleApplication extends Application[ScalaExampleConfiguration] {
}

object ScalaApplication extends ScalaExampleApplication with App {
  
  install((conf: ScalaExampleConfiguration) => new ThriftModule())
  
  run(args)

  def initialize(configuration: ScalaExampleConfiguration): Unit = {
    registerService(getResource(classOf[Thrift]))
  }
}


package io.herd.example.scala

import io.herd.server.Application
import io.herd.thrift.Thrift

trait ScalaExampleApplication extends Application[ScalaExampleConfiguration] {
}

object ScalaApplication extends ScalaExampleApplication with App {

  run(args)

  def initialize(configuration: ScalaExampleConfiguration): Unit = {
    registerService(
      createService("Thrift Servive", new Thrift()).listen(9090))
  }
}


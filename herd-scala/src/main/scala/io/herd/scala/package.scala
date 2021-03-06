package io.herd.scala

import java.util.function.{ Function ⇒ JFunction }

package object interop {

  //usage example: `i: Int ⇒ 42`
  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }
}
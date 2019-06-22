package org.jonnyzzz.jni.java


fun main() {

  //TODO: use correct path here
  val path = "/Users/jonnyzzz/Work/kotlin-jni-mix/build/bin/native/debugShared"

  System.setProperty("java.library.path", path)
  // reset java.library.path caches, see https://stackoverflow.com/a/24988095
  ClassLoader::class.java.getDeclaredField("sys_paths").apply {
    isAccessible = true
    set(null, null)
  }

  Runtime.getRuntime().loadLibrary("kotlin_jni_mix")

  val ret = NativeHost().callInt(42)
  println("ret from the native: $ret")
}

class NativeHost {
  external fun callInt(x: Int) : Int
}
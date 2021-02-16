package net.draycia.kterm.java

fun main() {
  Runtime.getRuntime().loadLibrary("KTerm")

  val handle = WindowHandle()
  handle.createWindow("Test", 640, 480)
}

class WindowHandle {
  external fun createWindow(name: String, width: Int, height: Int)
}

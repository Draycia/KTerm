package net.draycia.kterm.native

import kotlinx.cinterop.*
import net.draycia.kterm.*
import platform.windows.*
import platform.windows.NULL

@ExperimentalUnsignedTypes
fun WndProc(hwnd: HWND?, msg: UINT, wParam: WPARAM, lParam: LPARAM) : LRESULT
{
  // This switch block differentiates between the message type that could have been received. If you want to
  // handle a specific type of message in your application, just define it in this block.
  when(msg)
  {
    // This message type is used by the OS to close a window. Just closes the window using DestroyWindow(hwnd);
    WM_CLOSE.toUInt() -> DestroyWindow(hwnd)

    // This message type is part of the WM_CLOSE case. After the DestroyWindow(hwnd) function is called, a
    // WM_DESTROY message is sent to the window, which actually closes it.
    WM_DESTROY.toUInt() -> PostQuitMessage(0)

    // This message type is an important one for GUI programming. It symbolizes an event for a button for example.
    WM_COMMAND.toUInt() -> {
      // To differentiate between controls, compare the HWND of, for example, the button to the HWND that is passed
      // into the LPARAM parameter. This way you can establish control-specific actions.
      //if (lParam == button.objcPtr().toLong() && (wParam == BN_CLICKED.toULong()))
      //{
      //    // The button was clicked, this is your proof.
      //    MessageBoxA(hwnd, "Button is pressed!", "test", MB_ICONINFORMATION);
      //}
    }

    // When no message type is handled in your application, return the default window procedure. In this case the message
    // will be handled elsewhere or not handled at all.
    else -> return (DefWindowProc!!)(hwnd, msg, wParam, lParam)
  }

  return 0;
}

@Suppress("UNUSED_PARAMETER")
@CName("Java_net_draycia_kterm_java_WindowHandle_createWindow")
fun createWindow(env: CPointer<JNIEnvVar>, clazz: jclass, name: jstring, width: jint, height: jint) {
  initRuntimeIfNeeded()
  Platform.isMemoryLeakCheckerActive = false

  memScoped {

    val hInstance = (GetModuleHandle!!)(null)
    val lpszClassName = "GijSoft"

    // In order to be able to create a window you need to have a window class available. A window class can be created for your
    // application by registering one. The following struct declaration and fill provides details for a new window class.
    val wc = alloc<WNDCLASSEX>();

    wc.cbSize        = sizeOf<WNDCLASSEX>().toUInt();
    wc.style         = 0u;
    wc.lpfnWndProc   = staticCFunction(::WndProc);
    wc.cbClsExtra    = 0;
    wc.cbWndExtra    = 0;
    wc.hInstance     = hInstance;
    wc.hIcon         = null;
    wc.hCursor       = (LoadCursor!!)(hInstance, IDC_ARROW);
    //wc.hbrBackground = HBRUSH(COLOR_WINDOW+1);
    wc.lpszMenuName  = null;
    wc.lpszClassName = lpszClassName.wcstr.ptr
    wc.hIconSm       = null;

    // This function actually registers the window class. If the information specified in the 'wc' struct is correct,
    // the window class should be created and no error is returned.
    if((RegisterClassEx!!)(wc.ptr) == 0u.toUShort())
    {
      println("Failed to register!")
      return
    }

    // Convert jstring to KString
    val windowName = name.toKString(env)

    // This function creates the first window. It uses the window class registered in the first part, and takes a title,
    // style and position/size parameters. For more information about style-specific definitions, refer to the MSDN where
    // extended documentation is available.
    val hwnd = CreateWindowExA(WS_EX_CLIENTEDGE, lpszClassName, windowName,
            (WS_OVERLAPPED or WS_CAPTION or WS_SYSMENU or WS_MINIMIZEBOX).toUInt(),
            CW_USEDEFAULT, CW_USEDEFAULT, width, height, null, null, hInstance, NULL
    )

    // Everything went right, show the window including all controls.
    ShowWindow(hwnd, 1);

    println("Created window with name $windowName and size ($width, $height)")

    UpdateWindow(hwnd);

    // This part is the "message loop". This loop ensures the application keeps running and makes the window able to receive messages
    // in the WndProc function. You must have this piece of code in your GUI application if you want it to run properly.
    val Msg = alloc<MSG>();
    while((GetMessage!!)(Msg.ptr, null, 0u, 0u) > 0)
    {
      TranslateMessage(Msg.ptr);
      (DispatchMessage!!)(Msg.ptr);
    }
  }
}

fun jstring.toKString(env: CPointer<JNIEnvVar>) : String {
  return env.pointed.pointed!!.GetStringChars!!.invoke(env, this, null)!!.toKString()
}
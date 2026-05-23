package com.kangrio.shellserver

object Constants {
    const val TAG = "ShellServer"
    const val ACTION_RECEIVE_BINDER = "com.kangrio.shellserver.action.binder"
    const val ACTION_RECEIVE_BINDER_REQUEST = "com.kangrio.shellserver.action.binder.request"
    const val TRANSACTION_invodeSystemService = IShellServer.Stub.TRANSACTION_invodeSystemService
}
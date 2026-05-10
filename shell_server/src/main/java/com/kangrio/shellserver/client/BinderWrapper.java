package com.kangrio.shellserver.client;


import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kangrio.shellserver.Constants;
import com.kangrio.shellserver.client.utils.ServerUtil;

import java.io.FileDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BinderWrapper implements IBinder {
    private static final String TAG = "BinderWrapper";

    private final IBinder original;
    private static IBinder mRemote;

    public BinderWrapper(String serviceName) {
        this.original = ServerUtil.INSTANCE.getSystemService(serviceName);
        mRemote = ServerUtil.INSTANCE.getRemoteBinder();
    }

    public BinderWrapper(IBinder original) {
        this.original = original;
        mRemote = ServerUtil.INSTANCE.getRemoteBinder();
    }

    @Override
    public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        original.dump(fd, args);
    }

    @Override
    public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
        original.dumpAsync(fd, args);
    }

    @Nullable
    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return original.getInterfaceDescriptor();
    }

    @Override
    public boolean isBinderAlive() {
        return original.isBinderAlive();
    }

    @Override
    public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
        original.linkToDeath(recipient, flags);
    }

    @Override
    public boolean pingBinder() {
        return original.pingBinder();
    }

    @Nullable
    @Override
    public IInterface queryLocalInterface(@NonNull String descriptor) {
        return null;
    }

    @Override
    public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        Parcel newData = Parcel.obtain();
        try {
            newData.writeStrongBinder(original);
            newData.writeInt(code);
            newData.writeInt(flags);
            newData.appendFrom(data, 0, data.dataSize());
            mRemote.transact(Constants.TRANSACTION_invodeSystemService, newData, reply, 0);
        } finally {
            newData.recycle();
        }
        return true;
    }

    @Override
    public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
        return original.unlinkToDeath(recipient, flags);
    }
}

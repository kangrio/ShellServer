package android.net;

public interface INetworkPolicyManager {
    void addUidPolicy(int i, int i2) throws android.os.RemoteException;

    void disableNat(String str, String str2) throws android.os.RemoteException;

    void enableMobileForPenaltyBox(boolean z) throws android.os.RemoteException;

    void enableNat(String str, String str2) throws android.os.RemoteException;

    void enableWhiteRules(boolean z) throws android.os.RemoteException;

    void enableWifiForPenaltyBox(boolean z) throws android.os.RemoteException;

    void factoryReset(String str) throws android.os.RemoteException;

    boolean getRestrictBackground() throws android.os.RemoteException;

    int getRestrictBackgroundByCaller() throws android.os.RemoteException;

    android.telephony.SubscriptionPlan[] getSubscriptionPlans(int i, String str) throws android.os.RemoteException;

    String getSubscriptionPlansOwner(int i) throws android.os.RemoteException;

    int getUidPolicy(int i) throws android.os.RemoteException;

    int[] getUidsWithPolicy(int i) throws android.os.RemoteException;

    int[] getUidsWithWifiPolicy(int i) throws android.os.RemoteException;

    boolean getWhiteListStats() throws android.os.RemoteException;

    boolean isUidNetworkingBlocked(int i, boolean z) throws android.os.RemoteException;

    void onTetheringChanged(String str, boolean z) throws android.os.RemoteException;


    void removeUidPolicy(int i, int i2) throws android.os.RemoteException;

    void setDeviceIdleMode(boolean z) throws android.os.RemoteException;

    void setMeteredRestrictedUid(int i, boolean z) throws android.os.RemoteException;


    void setRestrictBackground(boolean z) throws android.os.RemoteException;

    void setSubscriptionOverride(int i, int i2, int i3, long j, String str) throws android.os.RemoteException;

    void setSubscriptionPlans(int i, android.telephony.SubscriptionPlan[] subscriptionPlanArr, String str) throws android.os.RemoteException;

    void setUidPolicy(int i, int i2) throws android.os.RemoteException;

    void setUidWhiteRules(int i, boolean z) throws android.os.RemoteException;

    void setUidWifiPolicy(int i, int i2) throws android.os.RemoteException;

    void setWifiMeteredOverride(String str, int i) throws android.os.RemoteException;


}

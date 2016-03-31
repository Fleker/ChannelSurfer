package com.felkertech.channelsurfer.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.felkertech.channelsurfer.R;

/**
 * Dummy account service for SyncAdapter. Note that this does nothing because this input uses a feed
 * which does not require any authentication.
 */
public class DummyAccountService extends Service {
    private static final String TAG = "DummyAccountService";
    private DummyAuthenticator mAuthenticator;
//    public static final String ACCOUNT_NAME = "";

    public static Account getAccount(Context mContext) {
        String ACCOUNT_NAME = mContext.getString(R.string.app_name);
        return new Account(ACCOUNT_NAME, ACCOUNT_NAME);
    }

    @Override
    public void onCreate() {
        mAuthenticator = new DummyAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    /**
     * Dummy Authenticator used in {@link SyncAdapter}. This does nothing for all the operations
     * since channel/program feed does not require any authentication.
     */
    public class DummyAuthenticator extends AbstractAccountAuthenticator {
        public DummyAuthenticator(Context context) {
            super(context);
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                     String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                 String s, String s2, String[] strings, Bundle options) throws NetworkErrorException {
            Log.d(TAG, "Trying to add an account");
            final AccountManager accountManager = AccountManager.get(getApplicationContext());
            String ACCOUNT_NAME = getApplicationContext().getString(R.string.app_name);
            Account[] accounts = accountManager.getAccountsByType(ACCOUNT_NAME);
            Log.d(TAG, accounts.length+"accounts");
            Log.d(TAG, accounts[0].toString());
            if(accounts.length > 0) {
                final Intent intent = new Intent(getApplicationContext(), DummyAccountIgnoreActivity.class);
                intent.putExtra(DummyAccountIgnoreActivity.INTENT_ACTION, DummyAccountIgnoreActivity.ACTION_ADD);
                final Bundle bundle = new Bundle();
                bundle.putParcelable(AccountManager.KEY_INTENT, intent);
                return bundle;
            } else {
                return null;
            }
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                         Account account, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                   Account account, String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAuthTokenLabel(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                        Account account, String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                  Account account, String[] strings) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }
        @Override
        public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response,
                                               Account account) throws NetworkErrorException {
            final Bundle result = new Bundle();
//            final Intent intent = new Intent(getApplicationContext(), DummyAccountIgnoreActivity.class);
//            intent.putExtra(DummyAccountIgnoreActivity.INTENT_ACTION, DummyAccountIgnoreActivity.ACTION_REMOVE);
            Log.d(TAG, "Somebody is trying to delete this account");
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
//            result.putParcelable(AccountManager.KEY_INTENT, intent);
            return result;
        }
    }

}

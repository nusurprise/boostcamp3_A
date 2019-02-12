package com.aone.menurandomchoice.repository;

import android.content.Intent;

import com.aone.menurandomchoice.repository.model.BaseResponse;
import com.aone.menurandomchoice.repository.network.APIHelper;
import com.aone.menurandomchoice.repository.network.APIRepository;
import com.aone.menurandomchoice.repository.network.NetworkResponseListener;
import com.aone.menurandomchoice.repository.network.model.AddressResponseBody;
import com.aone.menurandomchoice.repository.network.model.MenuLocationResponseBody;
import com.aone.menurandomchoice.repository.network.pojo.MenuLocation;
import com.aone.menurandomchoice.GlobalApplication;
import com.aone.menurandomchoice.repository.local.SqliteDatabaseHelper;
import com.aone.menurandomchoice.repository.local.SqliteDatabaseRepository;
import com.aone.menurandomchoice.repository.model.StoreDetail;
import com.aone.menurandomchoice.repository.oauth.KakaoLoginHelper;
import com.aone.menurandomchoice.repository.oauth.KakaoLoginRepository;
import com.aone.menurandomchoice.repository.oauth.OnKakaoLoginListener;
import com.aone.menurandomchoice.repository.oauth.OnKakaoLogoutListener;
import com.aone.menurandomchoice.repository.server.OnStoreUpdatedCheckListener;
import com.aone.menurandomchoice.repository.server.OnSignUpRequestListener;
import com.aone.menurandomchoice.repository.server.OnSignedUpCheckListener;
import com.aone.menurandomchoice.repository.server.OnStoreDetailRequestListener;
import com.aone.menurandomchoice.repository.server.ServerDataHelper;
import com.aone.menurandomchoice.repository.server.ServerDataRepository;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class DataRepository implements Repository {

    private static Repository repository;
    private KakaoLoginHelper kakaoLoginHelper;
    private ServerDataHelper serverDataHelper;
    private APIHelper apiHelper;
    private SqliteDatabaseHelper sqliteDatabaseHelper;

    @NonNull
    public static Repository getInstance() {
        if(repository == null) {
            repository = new DataRepository();
        }

        return repository;
    }

    private DataRepository() {
        kakaoLoginHelper = KakaoLoginRepository.getInstance();
        serverDataHelper = ServerDataRepository.getInstance();
        apiHelper = APIRepository.getInstance();
        sqliteDatabaseHelper = SqliteDatabaseRepository.getInstance();
    }

    @Override
    public void checkLoggedinAccount(@NonNull OnKakaoLoginListener onKakaoLoginListener) {
        kakaoLoginHelper.checkLoggedinAccount(onKakaoLoginListener);
}

    @Override
    public void executeDeviceKakaoAccountLogin(@NonNull OnKakaoLoginListener onKakaoLoginListener) {
        kakaoLoginHelper.executeDeviceKakaoAccountLogin(onKakaoLoginListener);
    }

    @Override
    public void executeOtherKakaoAccountLogin(@NonNull OnKakaoLoginListener onKakaoLoginListener) {
        kakaoLoginHelper.executeOtherKakaoAccountLogin(onKakaoLoginListener);
    }

    @Override
    public void executeKakaoAccountLogout(@NonNull OnKakaoLogoutListener onKakaoLogoutListener) {
        kakaoLoginHelper.executeKakaoAccountLogout(onKakaoLogoutListener);
    }

    @Override
    public boolean isNeedKakaoSDKLoginScreen(int requestCode, int resultCode, @Nullable Intent data) {
        return kakaoLoginHelper.isNeedKakaoSDKLoginScreen(requestCode, resultCode, data);
    }

    @Override
    public void requestSignedUpCheck(long userId, @NonNull OnSignedUpCheckListener onSignedUpCheckListener) {
        serverDataHelper.requestSignedUpCheck(userId, onSignedUpCheckListener);
    }

    @Override
    public void requestSignUp(long userId,@NonNull  String accessKey, @NonNull OnSignUpRequestListener onSignUpRequestListener) {
        serverDataHelper.requestSignUp(userId, accessKey, onSignUpRequestListener);
    }

    @Override
    public void executeLocationSearch(@NonNull String Qeury, @NonNull NetworkResponseListener<AddressResponseBody> networkResponseListener) {
        apiHelper.executeLocationSearch(Qeury, networkResponseListener);
    }

    @Override
    public void requestMenuLocation(@NonNull Map<String, String> queryMap, @NonNull NetworkResponseListener<MenuLocationResponseBody> networkResponseListener) {
        apiHelper.requestMenuLocation(queryMap, networkResponseListener);
    }

    @Override
    public void requestStoreDetail(@NonNull int storeIdx,
                                      @NonNull NetworkResponseListener<BaseResponse<StoreDetail>> networkResponseListener) {
        apiHelper.requestStoreDetail(storeIdx, networkResponseListener);
    }


    @Override
    public void loadStoreDetail(final int storeIdx, @NonNull final OnLoadStoreDetailListener onLoadStoreDetailListener) {

        final StoreDetail cachedStoreDetail = getStoreDetail();

        requestStoreDetail(storeIdx, new NetworkResponseListener<BaseResponse<StoreDetail>>() {
            @Override
            public void onError() {
                onLoadStoreDetailListener.onFailToLoadStoreDetail();
            }

            @Override
            public void onReceived(@NonNull BaseResponse<StoreDetail> response) {
                onLoadStoreDetailListener.onStoreDetailLoaded(response.getData());
            }
        });
        /*
        checkStoreUpdated(cachedStoreDetail.getTime(), new OnStoreUpdatedCheckListener() {
            @Override
            public void onAlreadyStoreUpdated() {
                onLoadStoreDetailListener.onStoreDetailLoaded(cachedStoreDetail);
            }

            @Override
            public void onNotUpdated(@NonNull OnStoreDetailRequestListener onStoreDetailRequestListener) {
                requestStoreDetail(storeIdx, onStoreDetailRequestListener);
            }
        });
        */

    }

    @Override
    public void checkStoreUpdated(@NonNull String updateTime, @NonNull OnStoreUpdatedCheckListener onStoreUpdatedCheckListener) {
        serverDataHelper.checkStoreUpdated(updateTime, onStoreUpdatedCheckListener);
    }

    @Override
    public void requestStoreDetail(int storeIdx, @NonNull OnStoreDetailRequestListener onStoreDetailRequestListener) {
        serverDataHelper.requestStoreDetail(storeIdx, onStoreDetailRequestListener);
    }

    @Override
    public void addStoreDetail() {
        sqliteDatabaseHelper.addStoreDetail();
    }

    @Override
    public StoreDetail getStoreDetail() {
        return sqliteDatabaseHelper.getStoreDetail();
    }

    @Override
    public void updateStoreDetail(@NonNull final StoreDetail storeDetail) {
        sqliteDatabaseHelper.updateStoreDetail(storeDetail);
    }

    @Override
    public void cancelAll() {
        // do cancel request
    }

}

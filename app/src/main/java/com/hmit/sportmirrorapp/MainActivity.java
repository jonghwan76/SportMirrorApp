package com.hmit.sportmirrorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


public class MainActivity extends AppCompatActivity {
    private WebView webView1;
    private WebSettings webSettings;
    private final Handler handler = new Handler();

    public static final int IMAGE_SELECTOR_REQ = 1;
    private ValueCallback mFilePathCallback;

    // 마지막으로 뒤로 가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로 가기 버튼을 누를 때 표시
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //환경설정
        webView1 = (WebView) findViewById(R.id.webView1);
        webSettings = webView1.getSettings();
        webSettings.setJavaScriptEnabled(true);         // 자바스크립트 사용
        webSettings.setAllowFileAccessFromFileURLs(true);   //파일 URL로 파일접근 허용 여부
        webSettings.setSupportMultipleWindows(true);    // 새창 띄우기 허용
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트 새창 띄우기 허용
        webSettings.setLoadWithOverviewMode(true);      // 메타태그 허용
        webSettings.setUseWideViewPort(true);           // 화면 사이즈 맞추기 허용
        webSettings.setSupportZoom(false);              // 화면줌 허용 여부
        webSettings.setBuiltInZoomControls(false);      // 화면 확대 축소 허용 여부
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);        // 브라우저 노캐쉬
        webSettings.setDomStorageEnabled(true);         // 로컬저장소 허용

        webView1.loadUrl("http://27.96.134.216:8080/");
//        webView1.loadUrl("http://10.0.2.2:8080/");  //로컬 서버 띄우기

        //웹뷰 파일 다운로드 처리
        webView1.setDownloadListener(new MyWebViewClient());

        //웹뷰에 크롬 사용 허용, 이 부분이 없으면 크롬에서 alert가 뜨지 않음
        webView1.setWebChromeClient(new WebChromeClient(){
            //이미지 업로드 갤러리 허용
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
                mFilePathCallback = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

                //한장의 사진 또는 동영상을 선택하는 경우
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("image/*");
//                intent.setType("video/*");
                intent.setType("image/* video/mp4");
                startActivityForResult(Intent.createChooser(intent, "Select picture"), IMAGE_SELECTOR_REQ);
                return true;
            }
        });  //웹뷰에 크롬 사용 허용. 이 부분이 없으면 크롬에서 alert가 뜨지 않음

        //주소창 숨김
        webView1.setWebViewClient(new WebViewClientClass());

        //자바스크립트에 대응할 함수를 정의한 클래스 붙여줌
        webView1.addJavascriptInterface(new AndroidBridge(), "token");
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //Log.d("WebViewClient URL : " , request.getUrl().toString());
            view.loadUrl(request.getUrl().toString());
            return true;
            //return super.shouldOverrideUrlLoading(view, request);
        }
    }

    //파일선택시 이미지 갤러리 열기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_SELECTOR_REQ) {
            if(resultCode == Activity.RESULT_OK) {
                if(resultCode == Activity.RESULT_OK) {
                    if(data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        Uri[] uris = new Uri[count];

                        for(int i=0;i<count;i++) {
                            uris[i] = data.getClipData().getItemAt(i).getUri();
                        }
                        mFilePathCallback.onReceiveValue(uris);
                    }
                    else if(data.getData() != null) {
                        mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
                    }
                }
            }
        }
    }

    //안드로이드 뒤로가기 버튼 정의
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 기존 뒤로 가기 버튼의 기능을 막기 위해 주석 처리 또는 삭제

        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지났으면 Toast 출력
        // 2500 milliseconds = 2.5 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finish();
            toast.cancel();
            toast = Toast.makeText(this,"이용해 주셔서 감사합니다.",Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /*
    @JavascriptInterface
    public void getToken(){
        String token = "1234"; //given_token
        webView1.loadUrl("javascript:setToken('" + token + "'");
    }
     */

    //안드로이드와 html 스크립트 연동
    private class AndroidBridge {
        @JavascriptInterface
        public void getToken(final String arg) { // 웹에서 전송한 문자열 데이터 입니다.

            //앱 토근 확인
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if(!task.isSuccessful()){
                                Log.w("FCM Log", "getInstanceId faild", task.getException());
                                return;
                            }
                            // Get new FCM registration token
                            String token = task.getResult();
                            System.out.println("FCM 토근 : " + token);
//                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();

                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("label", token);
                            clipboardManager.setPrimaryClip(clipData);
//                            Toast.makeText(getApplication(), "복사되었습니다.",Toast.LENGTH_LONG).show();

                            //웹 자바스크립트 호출
                            webView1.loadUrl("javascript:fncSetToken('"+ token +"')");
                        }
                    });
//            Log.d("Handler", arg);

        }
    }


    /* WebViewClient 를 상속받는 MyDownloadListener 클래스를 만들어 DownloadListener 를 구현해준다 */
    private class MyWebViewClient extends WebViewClient implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
            Log.d("Handler", "***** onDownloadStart()");
            Log.d("Handler", "***** onDownloadStart() - url : " + url);
            Log.d("Handler", "***** onDownloadStart() - userAgent : " + userAgent);
            Log.d("Handler", "***** onDownloadStart() - contentDisposition : " + contentDisposition);
            Log.d("Handler", "***** onDownloadStart() - mimeType : " + mimeType);

            // 파일명 잘라내기 및 확장자 확인
            String fileName = contentDisposition;
            if (fileName != null && fileName.length() > 0) {
                int idxFileName = fileName.indexOf("filename=");
                if (idxFileName > -1) {
                    fileName = fileName.substring(idxFileName + 9).trim();
                }
                if (fileName.endsWith(";")) {
                    fileName = fileName.substring(0, fileName.length() - 1);
                }
                if (fileName.startsWith("\"") && fileName.startsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }
            }else{
                // 파일명(확장자포함) 확인이 안되었을 때 기존방식으로 진행
                fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
            }

            //권한 체크
//          if(권한 여부) {
            //권한이 있으면 처리
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimeType);

            //------------------------COOKIE!!------------------------
            String cookies = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookies);
            //------------------------COOKIE!!------------------------

            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading file...");
//            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
            request.setTitle(fileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "파일을 다운로드합니다.", Toast.LENGTH_LONG).show();

//          } else {
            //권한이 없으면 처리
//          }
        }
    }


}
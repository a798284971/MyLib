package com.hgy.mylib;
/*
* 分享工具类
* */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.android.dingtalk.share.ddsharemodule.DDShareApiFactory;
import com.android.dingtalk.share.ddsharemodule.IDDShareApi;
import com.android.dingtalk.share.ddsharemodule.message.DDImageMessage;
import com.android.dingtalk.share.ddsharemodule.message.DDMediaMessage;
import com.android.dingtalk.share.ddsharemodule.message.DDWebpageMessage;
import com.android.dingtalk.share.ddsharemodule.message.SendMessageToDD;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class ShareUtil {
    private static IWXAPI mIWXinApi;
    private static String WEIXIN_APP_ID="";
    private static String DD_APP_ID = "";
    /**
     * 分享网页到朋友圈或者好友，视频和音乐的分享和网页大同小异，只是创建的对象不同。
     * 详情参考官方文档：
     * https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317340&token=&lang=zh_CN
     *
     * @param url         网页的url
     * @param title       显示分享网页的标题
     * @param description 对网页的描述
     * @param scene       分享方式：好友还是朋友圈 1朋友圈 0好友
     */
    public static boolean shareUrl(Context context, String url, String title, String imgUrl, String description, int scene) {
        Bitmap thumb  = null;
        if (imgUrl.contains("http")){
            try {
                Bitmap thumbBmp = BitmapFactory.decodeStream(new URL(imgUrl).openStream());

                thumb = Bitmap.createScaledBitmap(thumbBmp,105,145,true);
                thumbBmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            thumb = BitmapFactory.decodeFile(imgUrl);
        }
        //初始化一个WXWebpageObject对象，填写url
        WXWebpageObject webPage = new WXWebpageObject();
        webPage.webpageUrl = url;
        return share(context.getApplicationContext(), webPage, title, thumb, description, scene);
    }
    public static boolean shareUrl(Context context, String url, String title, int imgUrl, String description, int scene) {
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(),imgUrl);
        //初始化一个WXWebpageObject对象，填写url
        WXWebpageObject webPage = new WXWebpageObject();
        webPage.webpageUrl = url;
        return share(context.getApplicationContext(), webPage, title, thumb, description, scene);
    }


    /**
     * 分享图片到朋友圈或者好友
     *
     * @param bmp   图片的Bitmap对象
     * @param scene 分享方式：好友还是朋友圈
     */
    public static boolean sharePic(Context context, Bitmap bmp, int scene) {
            int i = bmp.getWidth();
            int j = bmp.getHeight();
            float dis = (i > j) ? (i / j) : (j / i);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
//        byte[] bytes = baos.toByteArray();

//        bmp.recycle();
//        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);



        Bitmap bit = bitmap2bit(bmp, 1.3*1024*1024);

        //Bitmap bm1 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        //初始化一个WXImageObject对象
//        WXImageObject imageObj = new WXImageObject(bytes);
//        imageObj.imageData = bytes;
//^
//        Bitmap bytesThump = bitmap2bit(bmp, 32*1024);
        //Bitmap thumb = BitmapFactory.decodeByteArray(bytesThump, 0, bytesThump.length);
//        bmp.recycle();
//        Bitmap thumb = Bitmap.createScaledBitmap(bmp, 150, 150, true);

//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap-xxhdpi-2016x1080.insight_cover);
        WXImageObject imgObj = new WXImageObject(bit);
//        bmp.recycle();
        return share(context, imgObj, bit, scene);
    }

    /**
     * 分享文字到朋友圈或者好友
     *
     * @param text  文本内容
     * @param scene 分享方式：好友还是朋友圈
     */
    public static boolean shareText(Context context, String text, int scene) {
        //初始化一个WXTextObject对象，填写分享的文本对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        return share(context, textObj, text, scene);
    }


    private static boolean share(Context context, WXMediaMessage.IMediaObject mediaObject, Bitmap thumb, int scene) {
        return share(context, mediaObject, null, thumb, null, scene);
    }

    private static boolean share(Context context, WXMediaMessage.IMediaObject mediaObject, String description, int scene) {
        return share(context, mediaObject, null, null, description, scene);
    }


    private static boolean share(Context context, WXMediaMessage.IMediaObject mediaObject, String title, Bitmap thumb, String description, int scene) {
        //初始化一个WXMediaMessage对象，填写标题、描述
        WXMediaMessage msg = new WXMediaMessage(mediaObject);
        if (title != null) {
            msg.title = title;
        }
        if (description != null) {
            msg.description = description;
        }
        if (thumb != null ) {
            if (mediaObject.getClass() != WXImageObject.class)
                msg.thumbData = bmpToByteArray(thumb, true);
            else {
//                Bitmap a = bitmap2bit(thumb,32*1024);
                Bitmap a = Bitmap.createScaledBitmap(thumb, 150, 150, true);
                msg.thumbData = bmpToByteArray(a,true);
            }
        }
        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = scene;

        mIWXinApi = WXAPIFactory.createWXAPI(context, WEIXIN_APP_ID,true);
        mIWXinApi.registerApp(WEIXIN_APP_ID);
        if (mIWXinApi.isWXAppInstalled()) {
            boolean b = mIWXinApi.sendReq(req);
            mIWXinApi = null;
            return b;
        } else {
            return false;
        }
    }
    //微信分享缩略图必须压缩后转化成byteArray提交
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        /*wx sdk中提供的方法，但是并不是很实用。
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;*/
        //采用canvas.drawBitmap压缩图片

        int i = bmp.getWidth();
        int j = bmp.getHeight();
        float dis = (i > j) ? (i / j) : (j / i);
        if (bmp.getHeight() > bmp.getWidth()) {
            i = 150;
            j = (int) (150*dis);
        } else {
            i = 150;
            j = (int) (150*dis);
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i, j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                //F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }


    public static byte[] bmpToByteArray2(final Bitmap bmp, final boolean needRecycle) {

        int i = bmp.getWidth();
        int j = bmp.getHeight();

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i, j), null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            } catch (Exception e) {
                //F.out(e);
            }
            i = bmp.getWidth();
            j = bmp.getHeight();
        }
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 10, output);
//        if (needRecycle) {
//            bmp.recycle();
//        }
//        byte[] result = output.toByteArray();
//        try {
//            output.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
    }

    public static byte[] bitmap2Bytes(Bitmap bitmap, int maxkb) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
             int options = 100;
             while (output.toByteArray().length > maxkb&& options != 10) {
                     output.reset(); //清空output
                     bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
                     options -= 5;
                 }
             return output.toByteArray();
    }

    public static Bitmap bitmap2bit(Bitmap bitmap, double maxkb) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        int options = 100;
        while (output.toByteArray().length > maxkb&& options != 10) {
            output.reset(); //清空output
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
            options -= 10;
        }
        return bitmap;
    }
    /*以下是分享到钉钉*/

    private static IDDShareApi iddShareApi ;

    public static boolean shareDDUrl(Context context, String url, String title, String imgUrl, String description, int lp){
        iddShareApi = DDShareApiFactory.createDDShareApi(context, DD_APP_ID, true);
        DDWebpageMessage webPageObject = new DDWebpageMessage();
        webPageObject.mUrl = url;

        DDMediaMessage webMessage = new DDMediaMessage();
        webMessage.mMediaObject = webPageObject;
        //填充网页分享必需参数，开发者需按照自己的数据进行填充
        webMessage.mTitle = title;
        webMessage.mContent = description;
        if (lp == 0){
            webMessage.mThumbUrl = imgUrl+"?x-oss-process=image/resize,m_fill,h_96,w_96";
        }else if (lp ==1){
            Bitmap thumb = null;
            try {
                Bitmap thumbBmp = BitmapFactory.decodeStream(new URL(imgUrl).openStream());

                thumb = Bitmap.createScaledBitmap(thumbBmp,105,145,true);
                thumbBmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (thumb!=null)
                webMessage.mThumbData = bmpToByteArray(thumb, true);
        }
        //构造一个Req
        SendMessageToDD.Req webReq = new SendMessageToDD.Req();
        webReq.mMediaMessage = webMessage;

        boolean isInstalled = iddShareApi.isDDAppInstalled();
        if (isInstalled) {
            return iddShareApi.sendReq(webReq);
        }else{
            return isInstalled;
        }
    }


    public static boolean shareDDImg(Context context, Bitmap bm){
        iddShareApi = DDShareApiFactory.createDDShareApi(context, DD_APP_ID, true);
        int i = bm.getWidth();
        int j = bm.getHeight();



        Bitmap bit = bitmap2bit(bm, 1.3*1024*1024);
        DDImageMessage imageObject = new DDImageMessage(bit);
        if (bm != null)
            bm.recycle();

        //构造一个DDMediaMessage对象
        DDMediaMessage mediaMessage = new DDMediaMessage();
        mediaMessage.mMediaObject = imageObject;

        //构造一个Req
        SendMessageToDD.Req req = new SendMessageToDD.Req();
        req.mMediaMessage = mediaMessage;
//        req.transaction = buildTransaction("image");

        boolean isInstalled = iddShareApi.isDDAppInstalled();
        if (isInstalled) {
            return iddShareApi.sendReq(req);
        }else{
            return isInstalled;
        }
    }


    public static boolean shareDDImg(Context context, String url){
        iddShareApi = DDShareApiFactory.createDDShareApi(context, DD_APP_ID, true);

        DDImageMessage imageObject = new DDImageMessage();
        imageObject.mImageUrl = url;

        //构造一个DDMediaMessage对象
        DDMediaMessage mediaMessage = new DDMediaMessage();
        mediaMessage.mMediaObject = imageObject;

        //构造一个Req
        SendMessageToDD.Req req = new SendMessageToDD.Req();
        req.mMediaMessage = mediaMessage;
//        req.transaction = buildTransaction("image");

        boolean isInstalled = iddShareApi.isDDAppInstalled();
        if (isInstalled) {
            return iddShareApi.sendReq(req);
        }else{
            return isInstalled;
        }
    }

}

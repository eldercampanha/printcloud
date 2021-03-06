package com.example.elder.printstop.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import android.webkit.WebView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Elder on 2016-05-02.
 */
public class PrintJobAdapter extends PrintDocumentAdapter {

    private String _fileName;
    private PrintJobAdapterInterface _interface;
    private InputStream input = null;
    private OutputStream output = null;
    private File path;
    private float valor;
    private int numberOfPages;

    public interface  PrintJobAdapterInterface{
        void onFinish(PrintJobAdapter adapter, float valor);
        void cancelled();
    }


    public PrintJobAdapter(String fileName, PrintJobAdapterInterface printJobAdapterInterface){
        _fileName = fileName;
        _interface = printJobAdapterInterface;
        Log.i("SSS","Construtor");
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback){


        try {
            path = new File(Environment.getExternalStoragePublicDirectory(Environment.MEDIA_MOUNTED),_fileName);
            input = new FileInputStream(path);
            output = new FileOutputStream(destination.getFileDescriptor());

            byte[] buf = new byte[1024];
            int bytesRead;

            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }

            cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                @Override
                public void onCancel() {
                    _interface.cancelled();
                }
            });
            Log.i("SSS","Print Job Adapter - onWrite");
            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

        } catch (FileNotFoundException ee){
            Log.i("SSS","Print Job Adapter - fail");
            //Catch exception
        } catch (Exception e) {
            Log.i("SSS","Print Job Adapter - fail");
            //Catch exception
        } finally {
            try {
                if( (input != null) && output != null) {
                    input.close();
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras){


        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            Log.i("SSS","Print Job Adapter - cancel");
            return;
        }

        PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(_fileName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

        callback.onLayoutFinished(pdi, true);
    }

    @Override
    public void onFinish() {
        Log.i("SSS","Print Job Adapter - onFinish");
        _interface.onFinish(this, valor);
        super.onFinish();
    }


}

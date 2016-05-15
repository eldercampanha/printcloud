package com.example.elder.printstop.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintJobInfo;
import android.print.PrintManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elder.printstop.AddMoreFiles;
import com.example.elder.printstop.R;
import com.example.elder.printstop.adapter.PrintJobAdapter;
import com.example.elder.printstop.adapter.RecyclerViewAdapterMainScreen;
import com.example.elder.printstop.async.DownloadPdfAsyncTask;
import com.example.elder.printstop.model.Cliente;
import com.example.elder.printstop.model.FileToPrint;
import com.example.elder.printstop.model.Pessoa;
import com.example.elder.printstop.singleton.ClienteEmEvidencia;
import com.example.elder.printstop.util.Downloader;
import com.example.elder.printstop.util.MyWebViewClient;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainScreen extends AppCompatActivity {

    private TextView txtName;
    private TextView txtSaldo;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewAdapterMainScreen mRecyclerViewAdapterMainScreen;
    private ProgressDialog progress;
    private WebView webview;
    private String printCloudInfoUrl = "https://lh4.ggpht.com/oZmXJ4CkYKdQonquHQekyI-5IpOo3D9ZVUX0pMvMWddX2PmhPAx_STgzZcw5Pa7Hcw=w300";
    public static FileToPrint selectedPDF;
    private Cliente _cliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        txtName = (TextView)findViewById(R.id.txt_user_name);
        txtSaldo = (TextView)findViewById(R.id.txt_user_saldo);
        progress = new ProgressDialog(this);
        progress.setTitle( "Loading Page..");
        progress.setMessage("Please wait");
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        _cliente = ClienteEmEvidencia.getInstance().getCliente();
        updateScreen();
        setWebView();
        setRecyclerView();
        setUserDataOnScreen();
//        loadWebView(printCloudInfoUrl);
    }

    private void updateScreen() {
        txtName.setText(ClienteEmEvidencia.getInstance().getCliente().getNome());
        txtSaldo.setText("R$ "+ ClienteEmEvidencia.getInstance().getCliente().getSaldo());
    }

    private void setUserDataOnScreen() {
    }

    public void setRecyclerView(){


        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view_main_files_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerViewAdapterMainScreen = new RecyclerViewAdapterMainScreen(this,
                ClienteEmEvidencia.getInstance().getCliente().getFiles(),
                new RecyclerViewAdapterMainScreen.RecyclerViewAdapterMainScreenInterface() {
                    @Override
                    public void fileCliked(int file) {
                        loadWebView(ClienteEmEvidencia.getInstance().getCliente().getFiles().get(file).getNome(), ClienteEmEvidencia.getInstance().getCliente().getCpf());
                    }
                });
        mRecyclerView.setAdapter(mRecyclerViewAdapterMainScreen);

    }

    public void setWebView(){


        webview = (WebView)findViewById(R.id.my_webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.enableSlowWholeDocumentDraw();

//        // Important: Only after the page is loaded we will do the print.
//        webview.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                doPrint(webview);
//            }
//        });
        webview.setWebViewClient(new MyWebViewClient(this,new MyWebViewClient.WebClienteInterface() {
            @Override
            public void start() {
                progress.show();
            }
            @Override
            public void finish() { progress.dismiss();}
        }));

    }
    public void loadWebView(String name, String cpf){
        try {
//            String url = "http://drive.google.com/viewerng/viewer?embedded=true&url=http://mycloudprinter.com.br/Files_Clientes/"+cpf+"/"+ name;
            String url = "http://drive.google.com/viewerng/viewer?embedded=true&url=http://mycloudprinter.com.br/adm/"+ name;
            Log.i("SSS", url);
            webview.loadUrl(url);
        } catch (Exception ex){
            Log.i("SSS", ex.getMessage());
            ex.printStackTrace();
        }
    }


    public void btnHelpClicked(View view){
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    public void btnAddMoreFilesClicked (View view){
        Intent intent = new Intent(this, AddMoreFiles.class);
        startActivity(intent);
    }

    public ArrayList<FileToPrint> getPdfFilesOnDevice() {

        ArrayList<FileToPrint> files = new ArrayList<>();
        String[] arquivos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).list();
        String pathUSB  = getApplicationContext().getExternalCacheDir().getAbsolutePath();
        Log.i("MainScreen", Environment.getExternalStorageDirectory().getAbsolutePath());// + APP_THUMBNAIL_PATH_SD_CARD;)
        Log.i("MainScreen", pathUSB);// + APP_THUMBNAIL_PATH_SD_CARD;)

        if(arquivos != null) {
            for (String s : arquivos) {
                FileToPrint file = new FileToPrint();
                file.setName(s);
                file.setFileSize(20);
                files.add(file);
            }
        }
        return files;
    }

    public void btnPrinterClicked(View view){
 //       createWebPrintJob(webview);
        methodPrintTest(webview);
    }

    public void methodPrintTest(WebView webView){

        new DownloadPdfAsyncTask().execute("http://maven.apache.org/maven-1.x/maven.pdf",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS +"teste.pdf"));
//      doPrint(webView);
//    webView.createPrintDocumentAdapter("teste");



//        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
//        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
//        String jobName = getString(R.string.app_name) + " Report ";
//
//        PrintAttributes printAttrs = new PrintAttributes.Builder().
//                setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME).
//                setMediaSize(PrintAttributes.MediaSize.NA_LETTER.asLandscape()).
//                setMinMargins(PrintAttributes.Margins.NO_MARGINS).
//                build();
//        PrintJob printJob = printManager.print(jobName, printAdapter,
//                printAttrs);
//        //Downloader.DownloadFile(webView.getUrl(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS +"teste.pdf"));
//        new DownloadPdfAsyncTask().execute("http://mycloudprinter.com.br/adm/Maven.pdf", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+"teste.pdf"));
    }

    private void createWebPrintJob(WebView webView) {

            // Get a PrintManager instance
            PrintManager printManager = (PrintManager) this
                    .getSystemService(Context.PRINT_SERVICE);

            // Get a print adapter instance
            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();
//            PrintDocumentAdapter printAdapter = new PrintJobAdapter(this);

            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Document";
            PrintJob printJob = printManager.print(jobName, printAdapter,
                    new PrintAttributes.Builder().build());

            PrintJobInfo info =  printJob.getInfo();
            // Save the job object for later status checking
         //   mPrintJobs.add(printJob);
    }


    public void btnLogoutClicked(View view){
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }



    private void doPrint(final WebView mWebView) {
        // Get the print manager.
        PrintManager printManager = (PrintManager) getSystemService(
                Context.PRINT_SERVICE);
        // Create a wrapper PrintDocumentAdapter to clean up when done.
        PrintDocumentAdapter adapter = new PrintDocumentAdapter() {
            private final PrintDocumentAdapter mWrappedInstance =
                    mWebView.createPrintDocumentAdapter();
            @Override
            public void onStart() {
                mWrappedInstance.onStart();
            }
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 CancellationSignal cancellationSignal, LayoutResultCallback callback,
                                 Bundle extras) {
                mWrappedInstance.onLayout(oldAttributes, newAttributes, cancellationSignal,
                        callback, extras);
            }
            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                CancellationSignal cancellationSignal, WriteResultCallback callback) {
                mWrappedInstance.onWrite(pages, destination, cancellationSignal, callback);
            }
            @Override
            public void onFinish() {
                mWrappedInstance.onFinish();
                // Intercept the finish call to know when printing is done
                // and destroy the WebView as it is expensive to keep around.
                mWebView.destroy();
                //mWebView = null;
            }
        };
        // Pass in the ViewView's document adapter.
        printManager.print("MotoGP stats", adapter, null);
    }
}

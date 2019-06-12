package hasar.com.hasarfiscallibrarygithub;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SplashActivity extends Activity {


    private static final int REQUEST_WRITE_STORAGE = 112;


    /**
     * Get the {@link Activity} to start when the splash screen times out.
     */
    @SuppressWarnings("rawtypes")
    public Class getNextActivityClass() {
        return MainActivity.class;
    }


    public String[] getRequiredPermissions() {
        String[] permissions = null;
        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (permissions == null) {
            return new String[0];
        } else {
            return permissions.clone();
        }
    }


    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /** Default creation code. */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        } else {
            startNextActivity();

        }
    }


    private void startNextActivity() {
        startActivity(new Intent(SplashActivity.this, getNextActivityClass()));
        finish();
    }


    private void checkPermissions() {
        String[] ungrantedPermissions = requiredPermissionsStillNeeded();

        if (ungrantedPermissions.length == 0) {
            startNextActivity();
        } else {
            requestPermission(ungrantedPermissions);
        }

    }

    public void requestPermission(String[] ungrantedPermissions) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : ungrantedPermissions) {
            result = ContextCompat.checkSelfPermission(getBaseContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE,
                            Manifest.permission.REQUEST_INSTALL_PACKAGES
                    },
                    REQUEST_WRITE_STORAGE);
        } else {

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        AssetManager assetManager = getAssets();
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        String[] filelist = assetManager.list("");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        copyDirorfileFromAssetManager("", "HasarFiscalTest");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    install_apk();
                } else {
                    Toast.makeText(this, "Habilitar los permisos, son obligatorios", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public String copyDirorfileFromAssetManager(String arg_assetDir, String arg_destinationDir) throws IOException {
        File sd_path = Environment.getExternalStorageDirectory();
        String dest_dir_path = sd_path + addLeadingSlash(arg_destinationDir);
        File dest_dir = new File(dest_dir_path);

        createDir(dest_dir);

        AssetManager asset_manager = getApplicationContext().getAssets();
        String[] files = asset_manager.list(arg_assetDir);

        for (int i = 0; i < files.length; i++) {


            if (files[i].contains("com.hasar.fiscaldriver-final.apk")) {

                // It is a file
                String dest_file_path = addTrailingSlash(dest_dir_path) + files[i];
                copyAssetFile(files[i], dest_file_path);
            }
        }

        return dest_dir_path;
    }


    public void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException {
        InputStream in = getApplicationContext().getAssets().open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    public String addTrailingSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            path += "/";
        }
        return path;
    }

    public String addLeadingSlash(String path) {
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    public void createDir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Can't create directory, a file is in the way");
            }
        } else {
            dir.mkdirs();
            if (!dir.isDirectory()) {
                throw new IOException("Unable to create directory");
            }
        }
    }


    /**
     * Convert the array of required permissions to a {@link Set} to remove
     * redundant elements. Then remove already granted permissions, and return
     * an array of ungranted permissions.
     */
    @TargetApi(23)
    private String[] requiredPermissionsStillNeeded() {

        Set<String> permissions = new HashSet<String>();
        for (String permission : getRequiredPermissions()) {
            permissions.add(permission);
        }
        for (Iterator<String> i = permissions.iterator(); i.hasNext(); ) {
            String permission = i.next();
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(SplashActivity.class.getSimpleName(),
                        "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
                    if (permission.equals("android.permission.REQUEST_INSTALL_PACKAGES") || permission.equals("android.permission.FOREGROUND_SERVICE"))
                        i.remove();
                }
                Log.d(SplashActivity.class.getSimpleName(),
                        "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }


    public void install_apk() {
        if (isSDKinstalled()) {
            startNextActivity();
            return;
        }

        File directory = Environment.getExternalStoragePublicDirectory("HasarFiscalTest");

        File toInstall = new File(directory, "com.hasar.fiscaldriver-final" + ".apk");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Uri apkUri = FileProvider.getUriForFile(this, "com.hasar.fiscaldriver" + ".provider", toInstall);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivity(intent);
        } else {
            Uri apkUri = Uri.fromFile(toInstall);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }


    }

    public boolean isPackageExisted(String targetPackage) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


    public boolean isSDKinstalled() {
        return isPackageExisted("com.hasar.fiscaldriver");
    }


}
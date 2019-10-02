package pansong291.xposed.quickenergy;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;

@SuppressWarnings("NullableProblems")
public class FileProvider extends ContentProvider {

    private static final String AUTHORITY = "pansong291.xposed.quickenergy.fileProvider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String path = getPath(uri);
        if (projection == null || projection.length == 0) {
            projection = new String[]{"_data", "mime_type", "_display_name", "_size", "date_modified"};
        }
        MatrixCursor cursor = new MatrixCursor(projection);
        if (path == null) {
            return cursor;
        }
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            return cursor;
        }
        int lastIndexOf = path.lastIndexOf(File.separatorChar) + 1;
        if (lastIndexOf < path.length()) {
            MatrixCursor.RowBuilder newRow = cursor.newRow();
            for (String str : projection) {
                switch (str) {
                    case "_data":
                        newRow.add(path);
                        break;
                    case "mime_type":
                        newRow.add(getType(path));
                        break;
                    case "_display_name":
                        newRow.add(lastIndexOf > 0 ? path.substring(lastIndexOf) : path);
                        break;
                    case "_size":
                        long length = file.length();
                        if (length >= 0) {
                            newRow.add(length);
                        } else {
                            newRow.add(0);
                        }
                        break;
                    case "date_modified":
                        long lastModified = file.lastModified();
                        if (lastModified >= 0) {
                            newRow.add(lastModified);
                        } else {
                            newRow.add(lastModified);
                        }
                        break;
                    default:
                        newRow.add(null);
                        break;
                }
            }
            return cursor;
        }
        throw new RuntimeException("No file name specified: ".concat(path));
    }

    @Override
    public String getType(Uri uri) {
        return getType(getPath(uri));
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        String path = getPath(uri);
        if (path != null) {
            return ParcelFileDescriptor.open(new File(path), getMode(mode));
        }
        throw new FileNotFoundException("Failed to find uri: " + uri.toString());
    }

    private static boolean checkAuthority(Uri uri) {
        if (uri == null) {
            return false;
        }
        return AUTHORITY.equals(uri.getAuthority());
    }

    private static String getPath(Uri uri) {
        return checkAuthority(uri) ? uri.getPath() : null;
    }

    private static String getMimeType(String suffix) {
        if ("log".equals(suffix)) {
            return "text/plain";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
    }

    private static String getType(String path) {
        if (path != null) {
            if (path.charAt(path.length() - 1) == File.separatorChar) {
                path = path.substring(0, path.length() - 1);
            }
            String suffix = null;
            if (path.length() > 0) {
                int lastIndexOf = path.lastIndexOf('.');
                if (lastIndexOf > -1) {
                    suffix = path.substring(lastIndexOf + 1);
                }
            }
            if (suffix != null) {
                String mime = getMimeType(suffix);
                if (mime != null) {
                    return mime;
                }
            }
        }
        return "*/*";
    }

    private static int getMode(String mode) {
        switch (mode) {
            case "r":
                return ParcelFileDescriptor.MODE_READ_ONLY;
            case "w":
            case "wt":
                return ParcelFileDescriptor.MODE_WRITE_ONLY
                        | ParcelFileDescriptor.MODE_CREATE
                        | ParcelFileDescriptor.MODE_TRUNCATE;
            case "wa":
                return ParcelFileDescriptor.MODE_WRITE_ONLY
                        | ParcelFileDescriptor.MODE_CREATE
                        | ParcelFileDescriptor.MODE_APPEND;
            case "rw":
                return ParcelFileDescriptor.MODE_READ_WRITE
                        | ParcelFileDescriptor.MODE_CREATE;
            case "rwt":
                return ParcelFileDescriptor.MODE_READ_WRITE
                        | ParcelFileDescriptor.MODE_CREATE
                        | ParcelFileDescriptor.MODE_TRUNCATE;
        }
        throw new IllegalArgumentException("Invalid mode: " + mode);
    }

    public static Uri getUri(Uri uri) {
        return uri.buildUpon().scheme("content").authority(AUTHORITY).build();
    }
}

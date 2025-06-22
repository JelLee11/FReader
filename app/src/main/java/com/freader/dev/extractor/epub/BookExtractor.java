package com.freader.dev.extractor.epub;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.freader.dev.db.library.BookModel;

import io.documentnode.epub4j.domain.Book;
import io.documentnode.epub4j.domain.Metadata;
import io.documentnode.epub4j.epub.EpubReader;
import io.documentnode.epub4j.domain.Resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.String;
import android.provider.DocumentsContract;
import android.content.ContentUris;
import android.database.Cursor;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class BookExtractor {

  private static final String ROOT_EPUB_FOLDER =
      Environment.getExternalStorageDirectory().getAbsolutePath() + "/FReader/";
  private static final String COVER_FOLDER = ROOT_EPUB_FOLDER + ".covers/";

  public List<BookModel> extractEpubMetadata(Context context, List<Uri> uris) {
    List<BookModel> bookList = new ArrayList<>();
    ContentResolver resolver = context.getContentResolver();

    File rootFolder = new File(ROOT_EPUB_FOLDER);
    if (!rootFolder.exists()) rootFolder.mkdir();
    File coverFolder = new File(COVER_FOLDER);
    if (!coverFolder.exists()) coverFolder.mkdir();

    for (Uri uri : uris) {
      try (InputStream in = resolver.openInputStream(uri)) {
        EpubReader reader = new EpubReader();
        Book book = reader.readEpub(in);
        Metadata metadata = book.getMetadata();
        
        
        // Get Book Actual Path
        String bookPath = getRealPathFromURI(context, uri);
        Log.d("BookPathExtracted", bookPath);
        // Extract title
        String title =
            metadata.getTitles().isEmpty() ? "Unknown Title" : metadata.getTitles().get(0);
        String coverPath = null;
        // Extract cover
        if (book.getCoverImage() != null) {
          byte[] coverBytes = book.getCoverImage().getData();
          String coverFileName = formattedTitle(title);
          File coverFile = new File(COVER_FOLDER, coverFileName);
          FileOutputStream fos = new FileOutputStream(coverFile);
          fos.write(coverBytes);
          fos.close();
          coverPath = coverFile.getAbsolutePath();
        }

        BookModel model = new BookModel(title, coverPath);
        model.setBookPath(bookPath);
        bookList.add(model);

      } catch (Exception err) {
        Log.e("BookExtractor", "Error processing EPUB: " + err.getMessage());
      }
    }

    return bookList;
  }

  private static String formattedTitle(String title) {
    return title.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("_+", "_") + ".jpg";
  }

    public String getRealPathFromURI(Context context, Uri uri) {
        String result = null;
    
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
    
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
    
            // DownloadsProvider
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
    
                return getDataColumn(context, contentUri, null, null);
    
            // MediaProvider
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
    
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
    
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{ split[1] };
    
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
    
        // MediaStore (and general)
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
    
        // File
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
    
        return result;
    }
    
    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
    
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}

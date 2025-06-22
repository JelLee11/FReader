package com.freader.dev.extractor.epub;

// EpubLocator.java

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EpubLocator {

  public interface Callback {
    void onEpubFilesFound(ArrayList<Uri> epubFiles);
  }

  public static void findEpubsAsync(Context context, Uri rootUri, Callback callback) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    executor.execute(
        () -> {
          ArrayList<Uri> epubFiles = new ArrayList<>();
          traverseFolder(context, rootUri, epubFiles);

          // Return result on the main thread
          mainHandler.post(() -> callback.onEpubFilesFound(epubFiles));
        });
  }

  private static void traverseFolder(Context context, Uri folderUri, ArrayList<Uri> result) {
    Uri childrenUri =
        DocumentsContract.buildChildDocumentsUriUsingTree(
            folderUri, DocumentsContract.getTreeDocumentId(folderUri));

    Cursor cursor =
        context
            .getContentResolver()
            .query(
                childrenUri,
                new String[] {
                  DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                  DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                  DocumentsContract.Document.COLUMN_MIME_TYPE
                },
                null,
                null,
                null);

    try {
      if (cursor != null) {
        while (cursor.moveToNext()) {
          String docId = cursor.getString(0);
          String name = cursor.getString(1);
          String mimeType = cursor.getString(2);

          Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId);

          if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
            traverseFolder(context, documentUri, result);
          } else if (name.toLowerCase().endsWith(".epub")) {
            result.add(documentUri);
          }
        }
      }
    } finally {
      if (cursor != null) cursor.close();
    }
  }
}

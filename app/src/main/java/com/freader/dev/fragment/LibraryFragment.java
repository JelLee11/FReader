package com.freader.dev.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.freader.dev.R;
import com.freader.dev.adapter.GridRowDividerDecoration;
import com.freader.dev.adapter.LibraryBookAdapter;
import com.freader.dev.db.library.BookModel;
import com.freader.dev.db.library.LibraryDatabaseHelper;
import com.freader.dev.extractor.epub.BookExtractor;
import com.freader.dev.extractor.epub.EpubLocator;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
import smith.lib.alerts.dialog.AlertSDialog;
import com.freader.dev.MainActivity;
import smith.lib.alerts.dialog.LoadingSDialog;
import smith.lib.alerts.dialog.SDialog;
import smith.lib.alerts.dialog.callbacks.OnClickCallback;

public class LibraryFragment extends Fragment {

  private CoordinatorLayout coordinator;
  private AppBarLayout appbar;
  private MaterialToolbar toolbar;
  private NestedScrollView scroll;
  private RecyclerView recyclerView;
  private LinearLayout errorLayout;
  private LinearLayout importFileButton, browseFileButton;
  private ImageView menuIcon;

  private LibraryDatabaseHelper libraryHelper;
  private ActivityResultLauncher<Intent> folderPickerLauncher;
  private static final int REQUEST_CODE_PICK_FOLDER = 1001;

  private LoadingSDialog loadingSDialog;
  private LibraryBookAdapter adapter;
  private List<BookModel> bookList = new ArrayList<>();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
    View view = inflater.inflate(R.layout.fragment_library, parent, false);
    initialize(bundle, view);
    initializeFolderPicker();
    initializeLogic();
    return view;
  }

  private void initialize(Bundle bundle, View view) {
    coordinator = view.findViewById(R.id.coordinator);
    appbar = view.findViewById(R.id.appbar);
    toolbar = view.findViewById(R.id.toolbar);
    scroll = view.findViewById(R.id.nested_scroll_view);
    recyclerView = view.findViewById(R.id.recyclerView);
    errorLayout = view.findViewById(R.id.errorLayout);
    importFileButton = view.findViewById(R.id.importFileButton);
    browseFileButton = view.findViewById(R.id.browseFileButton);
    menuIcon = view.findViewById(R.id.settingsButton);

    int spanCount = 3;
    int dividerHeight = dpToPx(15);
    int spaceTopDivider = dpToPx(16);
    int itemSpacing = dpToPx(8);

    recyclerView.addItemDecoration(
        new GridRowDividerDecoration(
            getContext(), spanCount, dividerHeight, spaceTopDivider, itemSpacing));

    appbar.bringToFront();
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity != null) {
      activity.setSupportActionBar(toolbar);
      if (activity.getSupportActionBar() != null) {
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setHomeButtonEnabled(false);
        activity.getSupportActionBar().setTitle("Library");
      }
    }

    libraryHelper = new LibraryDatabaseHelper(getContext());
    loadingSDialog = new LoadingSDialog(getContext());

    importFileButton.setOnClickListener(
        v -> {
          v.animate()
              .alpha(0f)
              .setDuration(100)
              .withEndAction(
                  () -> {
                    v.animate().alpha(1f).setDuration(100).start();
                    new Handler(Looper.getMainLooper())
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                showLoader();
                                pickFolder();
                              }
                            },
                            500);
                  })
              .start();
        });

    menuIcon.setOnClickListener(
        v -> {
          v.animate()
              .alpha(0f)
              .setDuration(100)
              .withEndAction(
                  () -> {
                    v.animate().alpha(1f).setDuration(100).start();
                    showPopupMenu();
                  })
              .start();
        });
  }

  private void initializeLogic() {
    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
    // Check library db
    if (!libraryHelper.hasBooks()) {
      errorLayout.setVisibility(View.VISIBLE);
      scroll.setVisibility(View.GONE);
    } else {
      errorLayout.setVisibility(View.GONE);
      scroll.setVisibility(View.VISIBLE);
      bookList = libraryHelper.getAllBooks();
      displayBookData(getContext(), bookList);
    }
  }

  private int dpToPx(int dp) {
    return Math.round(dp * Resources.getSystem().getDisplayMetrics().density);
  }

  private void displayBookData(Context context, List<BookModel> books) {
    if (books != null && !books.isEmpty()) {
      getActivity()
          .runOnUiThread(
              () -> {
                adapter = new LibraryBookAdapter(context, books, itemClicked -> {
                    Log.d("Book Path", itemClicked.getBookPath());
                    Toast.makeText(context, itemClicked.getBookPath(), Toast.LENGTH_LONG).show();
                });
                recyclerView.setAdapter(adapter);
              });
    } else {
      errorLayout.setVisibility(View.VISIBLE);
      scroll.setVisibility(View.GONE);
    }
  }

  private void initializeFolderPicker() {
    folderPickerLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                  Uri treeUri = data.getData();

                  if (!isAdded()) return;
                  Context context = getContext();
                  Activity activity = getActivity();
                  if (context == null || activity == null || activity.isFinishing()) return;

                  context
                      .getContentResolver()
                      .takePersistableUriPermission(
                          treeUri,
                          Intent.FLAG_GRANT_READ_URI_PERMISSION
                              | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                  // Handle EPUBs asynchronously
                  EpubLocator.findEpubsAsync(
                      context,
                      treeUri,
                      epubFiles -> {
                        if (!isAdded()) return;
                        Context innerContext = getContext();
                        Activity innerActivity = getActivity();
                        if (innerContext == null
                            || innerActivity == null
                            || innerActivity.isFinishing()) return;

                        innerActivity.runOnUiThread(
                            () -> {
                              BookExtractor bookExtractor = new BookExtractor();
                              bookList.clear();

                              List<BookModel> newBooks =
                                  bookExtractor.extractEpubMetadata(innerContext, epubFiles);

                              for (BookModel book : newBooks) {
                                libraryHelper.addBook(book);
                              }

                              bookList.addAll(newBooks);

                              if (adapter == null) {
                                displayBookData(innerContext, bookList);
                              } else {
                                adapter.setBooks(newBooks);
                              }

                              errorLayout.setVisibility(View.GONE);
                              scroll.setVisibility(View.VISIBLE);

                              // Dismiss the loader after a short delay
                              new Handler(Looper.getMainLooper())
                                  .postDelayed(
                                      () -> {
                                        if (isAdded()) dismissDialog();
                                      },
                                      1000);
                            });
                      });
                }
              }
            });
  }

  private void showPopupMenu() {
    View popupView = LayoutInflater.from(getContext()).inflate(R.layout.library_custom_popup, null);
    PopupWindow popupWindow =
        new PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true);
    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    popupWindow.setOutsideTouchable(true);
    popupWindow.showAsDropDown(menuIcon, 0, 0);

    LinearLayout importFilePopButton = popupView.findViewById(R.id.importFileMenu);
    LinearLayout browsePopButton = popupView.findViewById(R.id.browseMenu);

    importFilePopButton.setOnClickListener(
        v -> {
          v.animate()
              .alpha(0f)
              .setDuration(100)
              .withEndAction(
                  () -> {
                    v.animate().alpha(1f).setDuration(100).start();
                    new Handler(Looper.getMainLooper())
                        .postDelayed(
                            new Runnable() {
                              @Override
                              public void run() {
                                popupWindow.dismiss();
                                showLoader();
                                pickFolder();
                              }
                            },
                            500);
                  })
              .start();
        });
  }

  private void pickFolder() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    folderPickerLauncher.launch(Intent.createChooser(intent, "Select Folder"));
  }

  private void showLoader() {
    getActivity()
        .runOnUiThread(
            () -> {
              loadingSDialog.setTitle("Please wait");
              loadingSDialog.setText("Extracting metadata...");
              loadingSDialog.setTheme(SDialog.THEME_BY_SYSTEM);
              loadingSDialog.show();
            });
  }

  private void dismissDialog() {
    getActivity()
        .runOnUiThread(
            () -> {
              loadingSDialog.dismiss();
            });
  }

  private boolean isSafe() {
    return isAdded() && getActivity() != null && !getActivity().isFinishing();
  }
}

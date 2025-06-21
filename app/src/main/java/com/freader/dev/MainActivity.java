package com.freader.dev;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.freader.dev.databinding.ActivityMainBinding;
import com.freader.dev.db.library.LibraryDatabaseHelper;
import smith.lib.alerts.dialog.AlertSDialog;
import smith.lib.alerts.dialog.SDialog;
import smith.lib.alerts.dialog.callbacks.OnClickCallback;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;
  private LibraryDatabaseHelper libraryDB;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Inflate and get instance of binding
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    // set content view to binding's root
    setContentView(binding.getRoot());
    showAlertSDialog();
    initialize();
    initializeLogic();
  }

  private void initialize() {
    libraryDB = new LibraryDatabaseHelper(this);
  }

  private void initializeLogic() {
    checkLibrary();
  }

  private void checkLibrary() {
    boolean libraryEmpty = libraryDB.isDatabaseEmpty();
    if (libraryEmpty) {
      showAlertSDialog();
    } else {
    }
  }

  private void showAlertSDialog() {
    AlertSDialog dialog = new AlertSDialog(MainActivity.this);
    dialog.setTitle("Empty Library");
    dialog.setText(
        "You currently do not have any data on your library. Please import books to start!");
    dialog.setTheme(SDialog.THEME_BY_SYSTEM);
    dialog.setPositiveButton(
        "Import",
        new OnClickCallback() {
          @Override
          public void onClick() {
            // TODO: Implement this method
          }
        });
    dialog.setNegativeButton(
        "Browse",
        new OnClickCallback() {
          @Override
          public void onClick() {
            // TODO: Implement this method
          }
        });
    dialog.setNeutralButton(
        "Cancel",
        new OnClickCallback() {
          @Override
          public void onClick() {
            dialog.dismiss();
          }
        });
    dialog.show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.binding = null;
  }
}

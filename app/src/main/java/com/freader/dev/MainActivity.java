package com.freader.dev;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;
import com.freader.dev.adapter.MainPagerFramentAdapter;
import com.freader.dev.databinding.ActivityMainBinding;
import com.freader.dev.db.library.LibraryDatabaseHelper;
import smith.lib.alerts.dialog.AlertSDialog;
import smith.lib.alerts.dialog.SDialog;
import smith.lib.alerts.dialog.callbacks.OnClickCallback;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;
  private MainPagerFramentAdapter fragmentAdapter;
  private ViewPager2 pager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Inflate and get instance of binding
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    // set content view to binding's root
    setContentView(binding.getRoot());
    initialize();
    initializeLogic();
  }

  private void initialize() {
    fragmentAdapter = new MainPagerFramentAdapter(this);
    binding.viewpager.setAdapter(fragmentAdapter);
    binding.viewpager.setUserInputEnabled(true);
    binding.viewpager.setCurrentItem(0, true);
  }

  private void initializeLogic() {}

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.binding = null;
  }
}

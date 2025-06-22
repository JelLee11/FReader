package com.freader.dev.adapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.freader.dev.fragment.LibraryFragment;

public class MainPagerFramentAdapter extends FragmentStateAdapter {

    public MainPagerFramentAdapter(FragmentActivity activity) {
        super(activity);
    }
    
    @Override
    public int getItemCount() {
        return 1;
    }
    
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0: return new LibraryFragment();
            default: return new LibraryFragment();
        }
    }

}

package com.macindex.macindex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class FavouriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        this.setTitle(getResources().getString(R.string.menu_favourite));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_favourite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addFolderItem:
                // To be implemented
                break;
            case R.id.deleteFolderItem:
                // To be implemented
                break;
            case R.id.favouriteHelpItem:
                LinkLoadingHelper.startBrowser(null, "https://macindex.paizhang.info/favourites", this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}

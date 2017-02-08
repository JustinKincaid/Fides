package com.ibea.fides.utils;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.widget.Toast;

import com.ibea.fides.adapters.NewShiftSearchAdapter;

/**
 * Created by Alaina Traxler on 2/8/2017.
 */

public class CustomItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private NewShiftSearchAdapter mAdapter;

    public CustomItemTouchHelperCallback(NewShiftSearchAdapter _adapter) {
        mAdapter = _adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0,ItemTouchHelper.START | ItemTouchHelper.END);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Log.d(">>>>", String.valueOf(direction));
    }
}
